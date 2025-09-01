# Scroll Block - API Documentation

## Core Detection APIs

### DoomScrollDetector

The primary detection engine for identifying doom scrolling behavior.

#### Methods

```kotlin
fun analyzeScrollEvent(currentTime: Long, scrollPosition: Int = 0): Float
```
- **Purpose**: Analyzes individual scroll events
- **Parameters**: 
  - `currentTime`: Current timestamp in milliseconds
  - `scrollPosition`: Optional scroll position data
- **Returns**: Confidence score (0.0 to 1.0) indicating doom scrolling likelihood
- **Usage**: Call for each scroll event to build behavioral pattern

```kotlin
fun isDoomScrolling(): Boolean
```
- **Purpose**: Determines if current behavior constitutes doom scrolling
- **Returns**: Boolean indicating if blocking should be triggered
- **Algorithm**: Combines confidence score with rapid scroll detection

```kotlin
fun getSessionAnalysis(): SessionAnalysis
```
- **Purpose**: Provides comprehensive analysis of current session
- **Returns**: SessionAnalysis object with detailed metrics
- **Usage**: Called when ending sessions for analytics

```kotlin
fun resetSession()
```
- **Purpose**: Resets all session data for new app session
- **Usage**: Call when user switches apps or starts new session

### SmartContentDetector

Analyzes app content to determine blocking appropriateness.

#### Methods

```kotlin
fun analyzeContent(rootNode: AccessibilityNodeInfo?, packageName: String): ContentAnalysis
```
- **Purpose**: Analyzes current screen content
- **Parameters**:
  - `rootNode`: Root accessibility node of current screen
  - `packageName`: Package name of current app
- **Returns**: ContentAnalysis with blocking recommendation
- **Algorithm**: Uses heuristics and pattern matching to classify content

#### ContentAnalysis Structure
```kotlin
data class ContentAnalysis(
    val contentType: ContentType,
    val shouldBlock: Boolean,
    val confidence: Float
)
```

#### ContentType Enum
- `YOUTUBE_SHORTS`: YouTube Shorts feed
- `INSTAGRAM_REELS`: Instagram Reels content
- `SNAPCHAT_SPOTLIGHT`: Snapchat Spotlight feed
- `SHORT_FORM_VIDEO`: Generic short-form video content
- `INFINITE_FEED`: Infinite scrolling feeds
- `SEARCH_RESULTS`: Search result pages
- `MESSAGING`: Chat/messaging interfaces
- `FULL_VIDEO`: Full-length video content
- `UNKNOWN`: Unclassified content

### PatternRecognitionEngine

Machine learning engine for behavioral analysis and personalization.

#### Methods

```kotlin
fun analyzeUserPatterns(historicalSessions: List<SessionData>): PersonalizedThresholds
```
- **Purpose**: Creates personalized detection parameters
- **Parameters**: Historical session data for analysis
- **Returns**: Customized thresholds for individual user
- **Algorithm**: Statistical analysis of user's doom scrolling vs. productive sessions

```kotlin
fun predictDoomScrollingRisk(
    currentHour: Int,
    dayOfWeek: String,
    batteryLevel: Int,
    recentSessions: List<SessionData>
): Float
```
- **Purpose**: Predicts likelihood of doom scrolling based on context
- **Returns**: Risk score (0.0 to 1.0)
- **Usage**: Proactive blocking strategy adjustment

## Database APIs

### ScrollPatternDao

Manages scroll pattern data for machine learning.

```kotlin
suspend fun insertScrollPattern(pattern: ScrollPattern)
fun getRecentPatterns(packageName: String): Flow<List<ScrollPattern>>
fun getDoomScrollingSessions(since: Long): Flow<List<ScrollPattern>>
suspend fun getAverageScrollVelocity(packageName: String, since: Long): Float?
suspend fun cleanOldPatterns(cutoffTime: Long)
```

### SessionDataDao

Handles detailed session analytics.

```kotlin
suspend fun insertSession(session: SessionData)
fun getRecentSessions(packageName: String): Flow<List<SessionData>>
fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<SessionData>>
suspend fun getTotalBlockedScrolls(since: Long): Int
suspend fun getAverageSessionVelocity(packageName: String, since: Long): Float?
```

### BlockingRuleDao

Manages customizable blocking rules.

```kotlin
suspend fun insertRule(rule: BlockingRule)
suspend fun updateRule(rule: BlockingRule)
suspend fun getActiveRules(packageName: String): List<BlockingRule>
fun getAllRules(): Flow<List<BlockingRule>>
suspend fun toggleAppBlocking(packageName: String, isEnabled: Boolean)
```

## Service Integration

### EnhancedScrollAccessibility

Main accessibility service with advanced detection capabilities.

#### Key Methods

```kotlin
private fun handleScrollEvent(event: AccessibilityEvent)
```
- **Purpose**: Processes scroll events with pattern analysis
- **Algorithm**: 
  1. Content type detection
  2. Scroll pattern analysis
  3. Confidence score calculation
  4. Blocking decision and execution

```kotlin
private fun applyBlocking(packageName: String, contentType: ContentType, confidence: Float)
```
- **Purpose**: Executes appropriate blocking strategy
- **Parameters**: App context, content type, detection confidence
- **Strategies**: Varies based on user settings and risk assessment

## Performance APIs

### PerformanceOptimizer

Ensures minimal resource usage.

#### Methods

```kotlin
fun getPerformanceMetrics(): PerformanceMetrics
```
- **Purpose**: Provides current performance statistics
- **Returns**: Memory usage, cache size, optimization status
- **Usage**: Monitoring and debugging performance issues

```kotlin
fun cacheData(key: String, data: Any, expirationMs: Long = 300000L)
fun getCachedData(key: String): Any?
```
- **Purpose**: Efficient data caching with automatic expiration
- **Usage**: Reduce database queries and improve response times

### UsageStatsTracker

Integrates with Android's usage statistics.

#### Methods

```kotlin
fun getAppUsageStats(days: Int = 7): Map<String, AppUsageStats>
```
- **Purpose**: Retrieves system-level usage statistics
- **Returns**: Usage data for supported apps
- **Usage**: Correlate blocking effectiveness with actual usage reduction

```kotlin
fun calculateUsageReduction(packageName: String, days: Int = 7): Float
```
- **Purpose**: Calculates percentage reduction in app usage
- **Returns**: Percentage change compared to previous period
- **Usage**: Measure blocking effectiveness

## Configuration APIs

### SettingsStore

Manages user preferences and app configuration.

#### Existing Methods (Enhanced)
```kotlin
val instagramKey: Flow<Boolean>
val youtubeKey: Flow<Boolean>
val linkedinKey: Flow<Boolean>
val snapchatKey: Flow<Boolean>

suspend fun setInstagramKey(isEnabled: Boolean)
suspend fun setYoutubeKey(isEnabled: Boolean)
suspend fun setLinkedinKey(isEnabled: Boolean)
suspend fun setSnapchatKey(isEnabled: Boolean)
```

#### New Configuration Options
```kotlin
// Sensitivity settings
suspend fun setSensitivityLevel(level: Int) // 1-5 scale
fun getSensitivityLevel(): Flow<Int>

// Blocking strategy
suspend fun setBlockingStrategy(strategy: BlockingStrategy)
fun getBlockingStrategy(): Flow<BlockingStrategy>

// Session limits
suspend fun setSessionTimeLimit(minutes: Int)
fun getSessionTimeLimit(): Flow<Int>

// Advanced features
suspend fun setAdaptiveLearning(enabled: Boolean)
fun getAdaptiveLearning(): Flow<Boolean>
```

## Error Handling

### Exception Types

```kotlin
class DetectionException(message: String) : Exception(message)
class OverlayException(message: String) : Exception(message)
class DatabaseException(message: String) : Exception(message)
```

### Error Recovery Strategies

1. **Accessibility Service Disconnection**
   - Automatic reconnection attempts
   - Graceful degradation to basic blocking
   - User notification of service status

2. **Overlay Permission Revoked**
   - Fall back to navigation-based blocking
   - Request permission re-grant
   - Continue core functionality

3. **Database Errors**
   - Retry mechanisms for transient failures
   - Data integrity checks
   - Automatic recovery procedures

## Integration Examples

### Basic Usage

```kotlin
// Initialize detection engine
val detector = DoomScrollDetector()

// Analyze scroll event
val confidence = detector.analyzeScrollEvent(System.currentTimeMillis())

// Check if blocking should occur
if (detector.isDoomScrolling()) {
    // Apply blocking strategy
    overlayManager.showBlockingOverlay()
}
```

### Advanced Pattern Analysis

```kotlin
// Get user's historical patterns
val sessions = sessionDataDao.getRecentSessions(packageName).first()

// Analyze patterns for personalization
val thresholds = patternEngine.analyzeUserPatterns(sessions)

// Predict current risk
val risk = patternEngine.predictDoomScrollingRisk(
    currentHour = LocalTime.now().hour,
    dayOfWeek = LocalDate.now().dayOfWeek.toString(),
    batteryLevel = getBatteryLevel(),
    recentSessions = sessions
)

// Adjust blocking strategy
val strategy = patternEngine.getOptimalBlockingStrategy(thresholds, risk, packageName)
```

### Custom Content Detection

```kotlin
// Analyze current screen content
val contentAnalysis = contentDetector.analyzeContent(rootInActiveWindow, packageName)

// Make blocking decision based on content type
when (contentAnalysis.contentType) {
    ContentType.YOUTUBE_SHORTS -> applyStrictBlocking()
    ContentType.SEARCH_RESULTS -> allowContent()
    ContentType.MESSAGING -> allowContent()
    else -> applyModerateBlocking()
}
```

## Performance Guidelines

### Memory Usage
- Keep scroll event history under 30 seconds
- Limit pattern cache to 100 entries
- Clean database records older than 7 days
- Target maximum 50MB RAM usage

### Battery Optimization
- Batch accessibility events when possible
- Use efficient database queries
- Minimize background processing
- Target <2% battery impact

### Response Time
- Process scroll events within 100ms
- Show blocking overlays within 200ms
- Maintain smooth user experience
- Avoid blocking legitimate interactions

## Testing APIs

### Mock Data Generation

```kotlin
// Generate test scroll patterns
fun generateTestScrollPattern(isDoomScrolling: Boolean): ScrollPattern

// Create mock session data
fun createMockSession(sessionType: String, duration: Long): SessionData

// Simulate accessibility events
fun simulateScrollEvent(packageName: String, velocity: Float): AccessibilityEvent
```

### Performance Testing

```kotlin
// Measure detection latency
fun measureDetectionLatency(): Long

// Test memory usage under load
fun testMemoryUsage(eventCount: Int): PerformanceMetrics

// Validate blocking accuracy
fun testBlockingAccuracy(testCases: List<TestCase>): Float
```

## Security Considerations

### Data Protection
- All sensitive data encrypted at rest
- No network transmission of personal data
- Automatic data expiration policies
- Minimal data collection principles

### Permission Security
- Accessibility service limited to specific apps
- Overlay permissions used only for blocking
- No access to sensitive user content
- Transparent permission usage

### Code Security
- Input validation for all user data
- Secure handling of accessibility events
- Protection against injection attacks
- Regular security audits