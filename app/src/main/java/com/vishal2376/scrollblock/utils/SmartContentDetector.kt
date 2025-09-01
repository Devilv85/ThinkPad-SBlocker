package com.vishal2376.scrollblock.utils

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Smart content detector that identifies specific UI elements and content types
 * 
 * This class uses advanced heuristics to detect different types of content
 * and determine whether they should be blocked based on user intent
 */
class SmartContentDetector {
    
    companion object {
        private const val TAG = "SmartContentDetector"
        
        // Content type identifiers
        private val SHORTS_INDICATORS = listOf("shorts", "short", "reel", "story")
        private val FEED_INDICATORS = listOf("feed", "timeline", "home")
        private val SEARCH_INDICATORS = listOf("search", "result", "query")
        private val MESSAGE_INDICATORS = listOf("message", "chat", "dm", "direct")
    }
    
    /**
     * Analyzes accessibility node to determine content type and blocking recommendation
     * 
     * @param rootNode Root accessibility node of the current screen
     * @param packageName Package name of the current app
     * @return ContentAnalysis with blocking recommendation
     */
    fun analyzeContent(rootNode: AccessibilityNodeInfo?, packageName: String): ContentAnalysis {
        if (rootNode == null) {
            return ContentAnalysis(ContentType.UNKNOWN, false, 0.0f)
        }
        
        val contentType = detectContentType(rootNode, packageName)
        val shouldBlock = shouldBlockContent(contentType, rootNode)
        val confidence = calculateDetectionConfidence(rootNode, contentType)
        
        return ContentAnalysis(contentType, shouldBlock, confidence)
    }
    
    /**
     * Detects the type of content currently displayed
     */
    private fun detectContentType(rootNode: AccessibilityNodeInfo, packageName: String): ContentType {
        val nodeText = extractAllText(rootNode).lowercase()
        val viewIds = extractViewIds(rootNode)
        
        return when {
            containsAny(nodeText, SHORTS_INDICATORS) || containsAny(viewIds, SHORTS_INDICATORS) -> {
                when (packageName) {
                    "com.google.android.youtube" -> ContentType.YOUTUBE_SHORTS
                    "com.instagram.android" -> ContentType.INSTAGRAM_REELS
                    "com.snapchat.android" -> ContentType.SNAPCHAT_SPOTLIGHT
                    else -> ContentType.SHORT_FORM_VIDEO
                }
            }
            containsAny(nodeText, FEED_INDICATORS) -> ContentType.INFINITE_FEED
            containsAny(nodeText, SEARCH_INDICATORS) -> ContentType.SEARCH_RESULTS
            containsAny(nodeText, MESSAGE_INDICATORS) -> ContentType.MESSAGING
            detectVideoPlayer(rootNode) -> ContentType.FULL_VIDEO
            else -> ContentType.UNKNOWN
        }
    }
    
    /**
     * Determines if content should be blocked based on type and context
     */
    private fun shouldBlockContent(contentType: ContentType, rootNode: AccessibilityNodeInfo): Boolean {
        return when (contentType) {
            ContentType.YOUTUBE_SHORTS,
            ContentType.INSTAGRAM_REELS,
            ContentType.SNAPCHAT_SPOTLIGHT,
            ContentType.SHORT_FORM_VIDEO,
            ContentType.INFINITE_FEED -> true
            
            ContentType.SEARCH_RESULTS,
            ContentType.MESSAGING,
            ContentType.FULL_VIDEO -> false
            
            ContentType.UNKNOWN -> {
                // Use heuristics for unknown content
                detectInfiniteScrollPattern(rootNode)
            }
        }
    }
    
    /**
     * Detects infinite scroll patterns in unknown content
     */
    private fun detectInfiniteScrollPattern(rootNode: AccessibilityNodeInfo): Boolean {
        // Look for recycler views or list views with rapid updates
        val scrollableNodes = findScrollableNodes(rootNode)
        
        return scrollableNodes.any { node ->
            val childCount = node.childCount
            val hasRapidUpdates = childCount > 10 // Heuristic for infinite scroll
            val hasVideoContent = containsVideoElements(node)
            
            hasRapidUpdates && hasVideoContent
        }
    }
    
    /**
     * Finds all scrollable nodes in the accessibility tree
     */
    private fun findScrollableNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val scrollableNodes = mutableListOf<AccessibilityNodeInfo>()
        
        fun traverse(node: AccessibilityNodeInfo) {
            if (node.isScrollable) {
                scrollableNodes.add(node)
            }
            
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    traverse(child)
                    child.recycle()
                }
            }
        }
        
        traverse(rootNode)
        return scrollableNodes
    }
    
    /**
     * Detects video player elements
     */
    private fun detectVideoPlayer(rootNode: AccessibilityNodeInfo): Boolean {
        val videoIndicators = listOf("video", "player", "play", "pause", "seek")
        val nodeText = extractAllText(rootNode).lowercase()
        val viewIds = extractViewIds(rootNode)
        
        return containsAny(nodeText, videoIndicators) || containsAny(viewIds, videoIndicators)
    }
    
    /**
     * Checks if node contains video elements
     */
    private fun containsVideoElements(node: AccessibilityNodeInfo): Boolean {
        val videoClasses = listOf("VideoView", "TextureView", "SurfaceView")
        
        fun hasVideoClass(node: AccessibilityNodeInfo): Boolean {
            val className = node.className?.toString() ?: ""
            return videoClasses.any { className.contains(it, ignoreCase = true) }
        }
        
        if (hasVideoClass(node)) return true
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                if (hasVideoClass(child)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }
        
        return false
    }
    
    /**
     * Extracts all text content from accessibility tree
     */
    private fun extractAllText(rootNode: AccessibilityNodeInfo): String {
        val textBuilder = StringBuilder()
        
        fun traverse(node: AccessibilityNodeInfo) {
            node.text?.let { textBuilder.append(it).append(" ") }
            node.contentDescription?.let { textBuilder.append(it).append(" ") }
            
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    traverse(child)
                    child.recycle()
                }
            }
        }
        
        traverse(rootNode)
        return textBuilder.toString()
    }
    
    /**
     * Extracts view IDs from accessibility tree
     */
    private fun extractViewIds(rootNode: AccessibilityNodeInfo): String {
        val idBuilder = StringBuilder()
        
        fun traverse(node: AccessibilityNodeInfo) {
            node.viewIdResourceName?.let { idBuilder.append(it).append(" ") }
            
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    traverse(child)
                    child.recycle()
                }
            }
        }
        
        traverse(rootNode)
        return idBuilder.toString()
    }
    
    /**
     * Calculates confidence score for content detection
     */
    private fun calculateDetectionConfidence(
        rootNode: AccessibilityNodeInfo,
        contentType: ContentType
    ): Float {
        val nodeText = extractAllText(rootNode).lowercase()
        val viewIds = extractViewIds(rootNode).lowercase()
        
        var confidence = 0.5f // Base confidence
        
        // Increase confidence based on specific indicators
        when (contentType) {
            ContentType.YOUTUBE_SHORTS -> {
                if (viewIds.contains("shorts") || nodeText.contains("shorts")) confidence += 0.3f
                if (viewIds.contains("reel") || nodeText.contains("reel")) confidence += 0.2f
            }
            ContentType.INSTAGRAM_REELS -> {
                if (viewIds.contains("reel") || nodeText.contains("reel")) confidence += 0.3f
                if (viewIds.contains("explore") || nodeText.contains("explore")) confidence += 0.2f
            }
            ContentType.INFINITE_FEED -> {
                if (findScrollableNodes(rootNode).size > 1) confidence += 0.2f
                if (containsVideoElements(rootNode)) confidence += 0.1f
            }
            else -> {
                // For other types, maintain base confidence
            }
        }
        
        return minOf(1.0f, confidence)
    }
    
    /**
     * Helper function to check if text contains any of the indicators
     */
    private fun containsAny(text: String, indicators: List<String>): Boolean {
        return indicators.any { text.contains(it, ignoreCase = true) }
    }
}

/**
 * Different types of content that can be detected
 */
enum class ContentType {
    YOUTUBE_SHORTS,
    INSTAGRAM_REELS,
    SNAPCHAT_SPOTLIGHT,
    SHORT_FORM_VIDEO,
    INFINITE_FEED,
    SEARCH_RESULTS,
    MESSAGING,
    FULL_VIDEO,
    UNKNOWN
}

/**
 * Result of content analysis
 */
data class ContentAnalysis(
    val contentType: ContentType,
    val shouldBlock: Boolean,
    val confidence: Float
)

/**
 * Personalized thresholds for individual users
 */
data class PersonalizedThresholds(
    val velocityThreshold: Float,
    val durationThreshold: Long,
    val confidenceThreshold: Float,
    val adaptiveBlocking: Boolean
)