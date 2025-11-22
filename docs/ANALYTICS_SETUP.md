# Google Analytics Setup for Life Problem Solver

## Overview
This app is configured with Firebase Analytics to track user behavior and app performance. The analytics are integrated through a clean service layer that can be easily extended.

## Setup Instructions

### 1. Add google-services.json
1. Download the `google-services.json` file from your Firebase Console
2. Place it in the `app/` directory of your project
3. The file should be at: `app/google-services.json`

### 2. Build the Project
After adding the `google-services.json` file, build the project:
```bash
./gradlew build
```

## Analytics Events Tracked

The app automatically tracks the following events:

### Problem Management
- **Problem Added**: When a user creates a new problem
  - Parameters: category, priority
- **Problem Solved**: When a user marks a problem as resolved
  - Parameters: problem_id, category

### AI Features
- **AI Solution Requested**: When a user requests an AI-generated solution
  - Parameters: problem_category
- **Rate Limit Reached**: When the user hits the API rate limit
  - Parameters: none

### User Actions
- **API Key Added**: When a user adds their own API key
  - Parameters: none
- **Screen Views**: Automatic tracking of screen navigation
  - Parameters: screen_name, screen_class

### Error Tracking
- **Errors**: When errors occur in the app
  - Parameters: error_type, error_message

## Using Analytics in Your Code

### Inject AnalyticsService
```kotlin
@HiltViewModel
class YourViewModel @Inject constructor(
    private val analyticsService: AnalyticsService
) : ViewModel()
```

### Track Custom Events
```kotlin
// Track a problem being added
analyticsService.logProblemAdded(
    category = "Work",
    priority = "HIGH"
)

// Track AI solution request
analyticsService.logAiSolutionRequested("Personal")

// Track errors
analyticsService.logError("network_error", "Connection timeout")

// Track screen views
analyticsService.logScreenView("ProblemDetailScreen")
```

### Set User Properties
```kotlin
// Set user properties for segmentation
analyticsService.logUserProperty("user_type", "premium")
analyticsService.logUserProperty("app_version", "1.0.0")
```

## Firebase Console

### View Analytics
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to "Analytics" in the left sidebar
4. View real-time and historical data

### Key Metrics to Monitor
- **Active Users**: Daily and monthly active users
- **Problem Creation**: How often users add problems
- **AI Usage**: How often users request AI solutions
- **Rate Limit Hits**: How often users hit API limits
- **Error Rates**: App stability and user experience
- **Screen Engagement**: Which screens are most used

### Custom Dashboards
You can create custom dashboards in Firebase Analytics to track:
- User retention rates
- Feature adoption (AI solutions, custom API keys)
- Problem categories popularity
- Error patterns and resolution

## Privacy Considerations

### Data Collected
- App usage patterns
- Feature usage (problems, AI solutions)
- Error occurrences
- Screen navigation

### Data NOT Collected
- Personal problem content
- API keys (only usage patterns)
- User identification (unless explicitly provided)

### GDPR Compliance
- Analytics data is anonymized
- Users can opt out through device settings
- No personal information is collected without consent

## Troubleshooting

### Common Issues
1. **Analytics not showing**: Ensure `google-services.json` is in the correct location
2. **Events not appearing**: Check internet connection and Firebase configuration
3. **Build errors**: Verify Firebase dependencies are correctly added

### Debug Mode
To enable debug logging for analytics:
```kotlin
// In your Application class
FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
```

## Extending Analytics

### Adding New Events
1. Add a new method to `AnalyticsService`
2. Use appropriate Firebase Analytics event types
3. Include relevant parameters for analysis

### Custom Parameters
```kotlin
analytics.logEvent(FirebaseAnalytics.Event.CUSTOM_EVENT) {
    param("custom_parameter", "value")
    param("numeric_parameter", 123)
}
```

### Conversion Tracking
Set up conversion events for important user actions:
```kotlin
analyticsService.logEvent(FirebaseAnalytics.Event.PURCHASE) {
    param(FirebaseAnalytics.Param.VALUE, 9.99)
    param(FirebaseAnalytics.Param.CURRENCY, "USD")
}
``` 