# How to Add Screenshots to the User Guide

The user guide is now set up to display actual screenshots from your Android device. Follow these steps to add them:

## Quick Method: Using ADB Script

1. **Connect your Android device via USB**
2. **Enable USB Debugging** on your device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"
3. **Run the capture script:**
   ```bash
   ./scripts/capture-screenshots.sh
   ```
4. **Follow the prompts** - the script will guide you through capturing each screenshot

## Manual Method: Using Your Device

1. **Take screenshots on your device:**
   - Press Volume Down + Power button simultaneously
   - Or use the screenshot option in your device's quick settings

2. **Transfer screenshots to your computer:**
   - Connect device via USB
   - Copy screenshots from your device's gallery/DCIM folder
   - Or use ADB: `adb pull /sdcard/Pictures/Screenshots/ images/screenshots/`

3. **Rename files** to match these exact names:
   - `voice-input.png` - Microphone icon in top bar
   - `edit-text.png` - Text input fields
   - `submit-button.png` - Submit Problem button
   - `ai-generate-button.png` - Generate AI Solution button
   - `ai-solution-display.png` - Displayed AI solution
   - `calendar-view.png` - Calendar tab view
   - `problem-list.png` - Main problem list
   - `rating-system.png` - 5-star rating interface
   - `mark-resolved.png` - Mark as resolved button
   - `delete-button.png` - Delete button
   - `analytics-dashboard.png` - Analytics screen

4. **Place files in:** `images/screenshots/` directory

## Screenshot Requirements

- **Format:** PNG or JPG
- **Size:** Recommended 1080x1920px (portrait) or similar
- **Quality:** Clear and readable
- **Privacy:** Remove any personal/sensitive information before committing

## What to Capture

### 1. Voice Input (`voice-input.png`)
- Show the Add Problem screen
- Highlight the microphone icon in the top bar

### 2. Edit Text (`edit-text.png`)
- Show the Add Problem screen
- Show the title and description text fields

### 3. Submit Button (`submit-button.png`)
- Show the Add Problem screen
- Show the "Submit Problem" button at the bottom

### 4. AI Generate Button (`ai-generate-button.png`)
- Show the Problem Detail screen
- Show the "Generate AI Solution" button

### 5. AI Solution Display (`ai-solution-display.png`)
- Show the Problem Detail screen
- Show the displayed AI solution with bullet points

### 6. Calendar View (`calendar-view.png`)
- Show the Calendar tab
- Show problems organized by date

### 7. Problem List (`problem-list.png`)
- Show the main Problems tab
- Show the list of problems

### 8. Rating System (`rating-system.png`)
- Show the Problem Detail screen
- Show the 5-star rating interface below the AI solution

### 9. Mark Resolved (`mark-resolved.png`)
- Show the Problem Detail screen
- Show the checkmark icon in the top bar

### 10. Delete Button (`delete-button.png`)
- Show the Problem Detail screen
- Show the delete/trash icon in the top bar

### 11. Analytics Dashboard (`analytics-dashboard.png`)
- Show the Analytics screen
- Show the metrics and value cards

## After Adding Screenshots

1. **Test locally:**
   ```bash
   # Open guide.html in your browser to verify images load correctly
   open guide.html  # macOS
   # or
   xdg-open guide.html  # Linux
   ```

2. **Commit and push:**
   ```bash
   git add images/screenshots/
   git commit -m "Add app screenshots for user guide"
   git push origin main
   ```

3. **Verify on GitHub Pages:**
   - Visit: https://gunainvestor.github.io/GenAI_life-problem-solver/guide.html
   - Check that all screenshots display correctly

## Notes

- The guide will show placeholder icons if screenshots are missing
- Screenshots will automatically display once you add them to `images/screenshots/`
- Make sure to use the exact filenames listed above
- Keep file sizes reasonable (under 1MB each if possible)

