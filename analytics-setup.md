# Analytics Setup Guide for Life Problem Solver

## Google Tag Manager Setup

### ✅ Already Configured
Your Google Tag Manager is already set up with ID: `GTM-NT2MCXD6`

The tracking code has been properly implemented:
- **Head Section**: GTM script placed at the top of `<head>`
- **Body Section**: GTM noscript iframe placed immediately after `<body>`

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

## How to View Analytics in Google Tag Manager

### 1. Access Google Tag Manager
1. Go to [Google Tag Manager](https://tagmanager.google.com/)
2. Sign in with your Google account
3. Select your container: `GTM-NT2MCXD6`

### 2. View Real-Time Data
1. In GTM, go to **Preview** mode
2. Enter your website URL: `https://gunainvestor.github.io/GenAI_life-problem-solver`
3. Click **Start** to see real-time events

### 3. Set Up Google Analytics 4 (Recommended)
1. In GTM, go to **Tags** → **New**
2. Choose **Google Analytics: GA4 Configuration**
3. Enter your GA4 Measurement ID
4. Set trigger to **All Pages**
5. Publish the container

### 4. Create Custom Events
1. Go to **Tags** → **New**
2. Choose **Google Analytics: GA4 Event**
3. Configure for download tracking:
   - **Event Name**: `download_click`
   - **Event Parameters**: 
     - `event_category`: `engagement`
     - `event_label`: `{{Click Element}}`
   - **Trigger**: Custom event when download buttons are clicked

## Google Analytics 4 Integration

### 1. Create GA4 Property
1. Go to [Google Analytics](https://analytics.google.com/)
2. Create a new GA4 property for "Life Problem Solver"
3. Get your Measurement ID (starts with "G-")

### 2. Connect GTM to GA4
1. In GTM, create a new tag
2. Choose **Google Analytics: GA4 Configuration**
3. Enter your GA4 Measurement ID
4. Set trigger to **All Pages**
5. Publish the container

### 3. View Analytics Data
1. Go to Google Analytics
2. Navigate to **Reports** → **Realtime** → **Events**
3. You'll see live events as they happen

## Custom Reports in GA4

### 1. Download Tracking Report
1. Go to **Reports** → **Engagement** → **Events**
2. Look for:
   - `download_click` events
   - `apk_download` events
   - `section_view` events

### 2. Create Custom Dashboard
1. Go to **Reports** → **Library**
2. Create new dashboard
3. Add widgets for:
   - Total downloads
   - Download conversion rate
   - Button performance by location
   - Geographic data

## Key Metrics to Monitor

### 1. **Total Downloads**
- Count of `apk_download` events
- Track daily/weekly trends

### 2. **Conversion Rate**
- Downloads / Page Views
- Target: >5% conversion rate

### 3. **Button Performance**
- Which download buttons work best
- Hero vs Pricing vs Main download

### 4. **User Journey**
- How users navigate to download
- Time spent on page before download

### 5. **Geographic Data**
- Where downloads come from
- Focus marketing efforts

## Sample GA4 Queries

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

### 1. Google Analytics Alerts
1. Go to **Admin** → **Custom Alerts**
2. Create alerts for:
   - High download activity (>10 downloads/hour)
   - Unusual traffic patterns
   - Conversion rate drops

### 2. GTM Preview Mode
- Use GTM Preview mode to test events
- Verify all download buttons trigger events
- Check data layer pushes

## Testing Your Setup

### 1. Test Download Tracking
1. Open your website in GTM Preview mode
2. Click each download button
3. Verify events appear in the preview panel

### 2. Check Real-Time Data
1. Go to Google Analytics
2. Navigate to **Realtime** → **Events**
3. Click download buttons and watch events appear

### 3. Debug Mode
Add this to test tracking:
```javascript
// Enable debug mode in GTM
dataLayer.push({
    'event': 'debug_mode',
    'debug': true
});
```

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
1. **No data showing**: Check if GTM is properly installed
2. **Events not firing**: Verify JavaScript console for errors
3. **Delayed data**: GA4 has 24-48 hour delay for some reports

### Debug Steps
1. Check GTM Preview mode
2. Verify dataLayer pushes in console
3. Check GA4 real-time reports
4. Ensure proper triggers are set

## Next Steps

1. **Set up GA4 property** and connect to GTM
2. **Test the tracking** by clicking download buttons
3. **Check real-time reports** to verify data collection
4. **Set up custom dashboards** for key metrics
5. **Configure alerts** for important events

## Support

If you need help with analytics setup:
- Google Tag Manager Help Center
- Google Analytics Help Center
- GitHub Issues for technical support
- Community forums for best practices

## Current Status

✅ **GTM Container**: `GTM-NT2MCXD6` - Active
✅ **Tracking Code**: Properly implemented
✅ **Event Tracking**: Download clicks configured
✅ **Data Layer**: Events pushing correctly

🔄 **Next**: Connect GA4 property for detailed analytics 