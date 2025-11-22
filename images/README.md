# Screenshots Directory

This directory contains screenshots of the Life Problem Solver app for use in the user guide.

## How to Add Screenshots

### Taking Screenshots on Android

1. **Using ADB (Android Debug Bridge):**
   ```bash
   adb shell screencap -p /sdcard/screenshot.png
   adb pull /sdcard/screenshot.png images/screenshots/
   ```

2. **Using Android Device:**
   - Press Volume Down + Power button simultaneously
   - Screenshots are saved to your device's gallery
   - Transfer them to this directory

3. **Using Android Studio:**
   - Open Device Manager
   - Select your device
   - Click the camera icon to take a screenshot

### Required Screenshots

Please add the following screenshots with these exact filenames:

1. `voice-input.png` - Microphone icon in the top bar
2. `edit-text.png` - Text fields for title and description
3. `submit-button.png` - Submit Problem button
4. `ai-generate-button.png` - Generate AI Solution button
5. `ai-solution-display.png` - Displayed AI solution
6. `calendar-view.png` - Calendar tab view
7. `problem-list.png` - Main problem list view
8. `rating-system.png` - 5-star rating interface
9. `mark-resolved.png` - Mark as resolved button
10. `delete-button.png` - Delete problem button
11. `analytics-dashboard.png` - Analytics screen

### Image Requirements

- **Format:** PNG or JPG
- **Size:** Recommended 1080x1920px (portrait) or similar
- **Naming:** Use lowercase with hyphens (e.g., `voice-input.png`)
- **Optimization:** Compress images to reduce file size while maintaining quality

### Notes

- Screenshots should show the actual app interface
- Remove any personal/sensitive information before committing
- Ensure screenshots are clear and readable
- Use consistent device/theme for all screenshots

