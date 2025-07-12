# Analytics Setup Guide for Life Problem Solver

## Google Analytics Setup

### 1. Create Google Analytics Account
1. Go to [Google Analytics](https://analytics.google.com/)
2. Click "Start measuring"
3. Create a new account for "Life Problem Solver"
4. Create a new property for your website
5. Get your Measurement ID (starts with "G-")

### 2. Update the Tracking Code
Replace `G-XXXXXXXXXX` in the `index.html` file with your actual Measurement ID:

```html
<!-- Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=G-YOUR_ACTUAL_ID"></script>
<script>
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());
    gtag('config', 'G-YOUR_ACTUAL_ID');
</script>
```

## What We're Tracking

### Download Button Clicks
- **Hero Section**: `trackDownloadClick('hero')`
- **Pricing Free**: `trackDownloadClick('pricing_free')`
- **Pricing Pro**: `trackDownloadClick('pricing_pro')`
- **Main Download**: `trackDownloadClick('main_download')`

### Events Tracked
1. **download_click** - When any download button is clicked
2. **apk_download** - Specific APK download event
3. **section_view** - When users view the download section

## How to View Analytics

### 1. Real-Time Reports
1. Go to Google Analytics
2. Navigate to **Reports** → **Realtime** → **Events**
3. You'll see live events as they happen

### 2. Event Reports
1. Go to **Reports** → **Engagement** → **Events**
2. Look for:
   - `download_click` events
   - `apk_download` events
   - `section_view` events

### 3. Custom Reports
Create custom reports to track:
- **Download Conversion Rate**: Downloads / Page Views
- **Button Performance**: Which buttons get more clicks
- **User Journey**: How users navigate to download

## Alternative Analytics Options

### 1. Simple Analytics (Privacy-Friendly)
```html
<script async defer src="https://scripts.simpleanalyticscdn.com/latest.js"></script>
<noscript><img src="https://queue.simpleanalyticscdn.com/noscript.gif" alt="" referrerpolicy="no-referrer-when-downgrade" /></noscript>
```

### 2. Plausible Analytics
```html
<script defer data-domain="gunainvestor.github.io" src="https://plausible.io/js/script.js"></script>
```

### 3. Self-Hosted Analytics
- **Matomo**: Open-source analytics platform
- **Umami**: Simple, privacy-focused analytics

## Quick Analytics Dashboard

### Key Metrics to Monitor
1. **Total Downloads**: Count of `apk_download` events
2. **Conversion Rate**: Downloads / Page Views
3. **Button Performance**: Which download buttons work best
4. **Geographic Data**: Where downloads come from
5. **Device Data**: Mobile vs Desktop downloads

### Sample Queries for Google Analytics 4
```sql
-- Total downloads
SELECT COUNT(*) as total_downloads
FROM `your-project.analytics_123456789.events_*`
WHERE event_name = 'apk_download'

-- Downloads by location
SELECT 
  event_label,
  COUNT(*) as downloads
FROM `your-project.analytics_123456789.events_*`
WHERE event_name = 'apk_download'
GROUP BY event_label
ORDER BY downloads DESC

-- Daily download trend
SELECT 
  DATE(event_timestamp) as date,
  COUNT(*) as downloads
FROM `your-project.analytics_123456789.events_*`
WHERE event_name = 'apk_download'
GROUP BY date
ORDER BY date
```

## Setting Up Alerts

### Google Analytics Alerts
1. Go to **Admin** → **Custom Alerts**
2. Create alerts for:
   - High download activity
   - Unusual traffic patterns
   - Conversion rate drops

### Example Alert
- **Alert Name**: High Download Activity
- **Condition**: Downloads > 10 in 1 hour
- **Action**: Email notification

## Privacy Considerations

### GDPR Compliance
- Add cookie consent banner
- Provide opt-out mechanism
- Document data collection practices

### Cookie Notice Example
```html
<div id="cookie-notice" class="cookie-notice">
    <p>We use cookies and analytics to improve your experience. 
    <a href="#" onclick="acceptCookies()">Accept</a> | 
    <a href="#" onclick="declineCookies()">Decline</a></p>
</div>
```

## Troubleshooting

### Common Issues
1. **No data showing**: Check if tracking code is properly installed
2. **Events not firing**: Verify JavaScript console for errors
3. **Delayed data**: Google Analytics has 24-48 hour delay for some reports

### Debug Mode
Add this to test tracking:
```javascript
// Enable debug mode
gtag('config', 'G-YOUR_ID', {
    debug_mode: true
});
```

## Next Steps

1. **Set up Google Analytics** with your Measurement ID
2. **Test the tracking** by clicking download buttons
3. **Check real-time reports** to verify data collection
4. **Set up custom dashboards** for key metrics
5. **Configure alerts** for important events

## Support

If you need help with analytics setup:
- Google Analytics Help Center
- GitHub Issues for technical support
- Community forums for best practices 