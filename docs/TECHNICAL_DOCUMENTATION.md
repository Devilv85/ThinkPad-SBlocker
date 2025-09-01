# Scroll Block - Technical Documentation

## Architecture Overview

The Scroll Block application implements a sophisticated multi-layered architecture for detecting and preventing doom scrolling behavior while preserving productive app usage.

### Core Components

#### 1. Detection Engine (`DoomScrollDetector.kt`)
- **Purpose**: Real-time behavioral analysis of scroll patterns
- **Algorithm**: Weighted scoring system using velocity, consistency, duration, and context
- **Key Features**:
  - Scroll velocity calculation (scrolls per second)
  - Pattern consistency analysis using statistical variance
  - Context-aware scoring based on time of day
  - Adaptive thresholds based on user behavior

#### 2. Content Analysis (`SmartContentDetector.kt`)
- **Purpose**: Identifies content types to distinguish productive vs. addictive content
- **Detection Methods**:
  - Accessibility tree analysis
  - View ID pattern matching
  - Text content analysis
  - UI element classification
- **Content Types**: Shorts/Reels, Infinite Feeds, Search Results, Messaging, Full Videos

#### 3. Pattern Recognition (`PatternRecognitionEngine.kt`)
- **Purpose**: Machine learning-based personalization of detection thresholds
- **Features**:
  - Historical behavior analysis
  - Personalized threshold calculation
  - Risk prediction based on context
  - Adaptive blocking strategy selection

#### 4. Overlay Management (`OverlayManager.kt`)
- **Purpose**: Non-intrusive blocking interface
- **Implementation**: System overlay windows with customizable messages
- **Strategies**: Gentle nudges, moderate blocking, aggressive intervention

### Database Schema

#### Enhanced Tables
```sql
-- Existing tables maintained for compatibility
AppUsage (usageId, packageName, date, scrollCount, timeSpent, appOpenCount, scrollsBlocked, createdAt, updatedAt)
Summary (summaryId, date, totalScrollCount, totalTimeSpent, totalAppOpenCount, totalScrollsBlocked, createdAt, updatedAt)

-- New advanced analytics tables
ScrollPattern (patternId, packageName, sessionStartTime, sessionEndTime, scrollVelocity, scrollDirection, consecutiveScrolls, pauseDuration, isDoomScrolling, confidenceScore, createdAt)

BlockingRule (ruleId, packageName, contentType, isEnabled, sensitivityLevel, maxSessionTime, cooldownPeriod, allowedTimeSlots, customViewIds, createdAt, updatedAt)

SessionData (sessionId, packageName, startTime, endTime, totalScrolls, blockedScrolls, averageScrollVelocity, maxConsecutiveScrolls, sessionType, exitMethod, batteryLevel, timeOfDay, dayOfWeek, createdAt)
```

### Detection Algorithm

#### Confidence Score Calculation
The doom scrolling detection uses a weighted scoring system:

```
Confidence = (Velocity × 0.3) + (Consistency × 0.25) + (Duration × 0.2) + (Pause × 0.15) + (Context × 0.1)
```

**Components:**
1. **Velocity Score**: Based on scrolls per second vs. threshold
2. **Consistency Score**: Statistical analysis of scroll timing patterns
3. **Duration Score**: Session length vs. typical doom scrolling duration
4. **Pause Score**: Time between scrolls (shorter = higher score)
5. **Context Score**: Time of day, battery level, recent usage patterns

#### Blocking Strategies
- **Minimal**: Gentle nudges only
- **Gentle**: Short overlays with tips
- **Moderate**: Blocking overlays with easy bypass
- **Aggressive**: Navigation blocking with cooldown periods

### Performance Optimizations

#### Memory Management
- Automatic cleanup of old scroll data (30-second rolling window)
- Database record expiration (7-day retention)
- Memory cache with LRU eviction
- Target: <50MB RAM usage

#### Battery Optimization
- Event batching to reduce CPU wake-ups
- Efficient accessibility event filtering
- Background task optimization
- Target: <2% battery impact

#### Processing Efficiency
- Minimum 100ms interval between scroll event processing
- Lazy loading of historical data
- Asynchronous database operations
- Smart caching of frequently accessed data

### Security and Privacy

#### Data Protection
- All data stored locally (no cloud transmission)
- Automatic data expiration
- No personal content analysis
- Minimal permission requirements

#### Accessibility Service Security
- Restricted to specific app packages
- No sensitive data extraction
- Secure overlay implementation
- Process isolation

### Integration Points

#### Android System Services
1. **AccessibilityService**: Core UI interaction detection
2. **UsageStatsManager**: App usage time tracking
3. **WindowManager**: Overlay display management
4. **NotificationManager**: Background service notifications

#### Third-party Integrations
- Room Database for local storage
- Hilt for dependency injection
- Jetpack Compose for modern UI
- Material 3 design system

### Testing Strategy

#### Unit Tests
- Detection algorithm accuracy tests
- Pattern recognition validation
- Performance benchmark tests
- Edge case handling verification

#### Integration Tests
- Accessibility service interaction
- Database operation validation
- UI overlay functionality
- Cross-app behavior testing

#### Manual Testing Procedures
1. Install app on test device
2. Enable accessibility service
3. Open supported apps (Instagram, YouTube, LinkedIn, Snapchat)
4. Perform various scroll patterns:
   - Rapid doom scrolling
   - Intentional content browsing
   - Search and messaging usage
5. Verify blocking accuracy and user experience
6. Monitor performance metrics

### Deployment Considerations

#### Google Play Store Compliance
- Accessibility service usage clearly explained
- Privacy policy for data handling
- Appropriate permission requests
- User consent for overlay permissions

#### Device Compatibility
- Android 6.0+ (API 23+) support
- Various screen sizes and densities
- Different Android OEM customizations
- Accessibility service variations

### Monitoring and Analytics

#### Performance Metrics
- Memory usage tracking
- Battery impact measurement
- Detection accuracy rates
- User engagement metrics

#### Error Handling
- Comprehensive exception catching
- Graceful degradation strategies
- User-friendly error messages
- Crash reporting integration

### Future Enhancements

#### Planned Features
- Machine learning model improvements
- Additional app support
- Advanced customization options
- Usage pattern insights
- Social features for accountability

#### Scalability Considerations
- Modular architecture for easy extension
- Plugin system for new app support
- Cloud sync capabilities (optional)
- Multi-device coordination