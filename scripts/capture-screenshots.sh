#!/bin/bash

# Script to capture screenshots from Android device via ADB
# Make sure your device is connected and USB debugging is enabled

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    print_error "ADB (Android Debug Bridge) is not installed or not in PATH"
    echo "Please install Android SDK Platform Tools:"
    echo "  macOS: brew install android-platform-tools"
    echo "  Linux: sudo apt-get install android-tools-adb"
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    print_error "No Android device connected or USB debugging not enabled"
    echo ""
    echo "To enable USB debugging:"
    echo "  1. Go to Settings → About Phone"
    echo "  2. Tap 'Build Number' 7 times"
    echo "  3. Go back to Settings → Developer Options"
    echo "  4. Enable 'USB Debugging'"
    echo "  5. Connect your device via USB"
    exit 1
fi

print_status "Android device detected!"

# Create screenshots directory if it doesn't exist
mkdir -p images/screenshots

# Screenshot mapping
declare -A screenshots=(
    ["voice-input"]="Take a screenshot showing the microphone icon in the top bar of Add Problem screen"
    ["edit-text"]="Take a screenshot showing the text input fields (title and description) in Add Problem screen"
    ["submit-button"]="Take a screenshot showing the 'Submit Problem' button at the bottom"
    ["ai-generate-button"]="Take a screenshot showing the 'Generate AI Solution' button in Problem Detail screen"
    ["ai-solution-display"]="Take a screenshot showing the displayed AI solution with bullet points"
    ["calendar-view"]="Take a screenshot showing the Calendar tab with problems organized by date"
    ["problem-list"]="Take a screenshot showing the main problem list view"
    ["rating-system"]="Take a screenshot showing the 5-star rating interface below AI solution"
    ["mark-resolved"]="Take a screenshot showing the checkmark icon to mark problem as resolved"
    ["delete-button"]="Take a screenshot showing the delete/trash icon in problem detail screen"
    ["analytics-dashboard"]="Take a screenshot showing the Analytics dashboard with metrics"
)

print_info "Ready to capture screenshots!"
echo ""
echo "Screenshots needed:"
for key in "${!screenshots[@]}"; do
    echo "  - $key.png: ${screenshots[$key]}"
done
echo ""

read -p "Press Enter when you're ready to capture the first screenshot, or Ctrl+C to exit..."

# Function to capture a screenshot
capture_screenshot() {
    local filename=$1
    local description=$2
    
    print_info "Preparing to capture: $filename.png"
    print_info "Description: $description"
    echo ""
    read -p "Navigate to the screen and press Enter to capture..."
    
    # Capture screenshot
    adb shell screencap -p /sdcard/temp_screenshot.png
    
    # Pull screenshot
    adb pull /sdcard/temp_screenshot.png images/screenshots/$filename.png
    
    # Clean up device
    adb shell rm /sdcard/temp_screenshot.png
    
    if [ -f "images/screenshots/$filename.png" ]; then
        print_status "Screenshot saved: images/screenshots/$filename.png"
        
        # Get file size
        size=$(du -h "images/screenshots/$filename.png" | cut -f1)
        print_info "File size: $size"
    else
        print_error "Failed to capture screenshot"
    fi
    
    echo ""
}

# Capture all screenshots
for key in "${!screenshots[@]}"; do
    capture_screenshot "$key" "${screenshots[$key]}"
done

print_status "All screenshots captured!"
echo ""
print_info "Screenshots saved in: images/screenshots/"
echo ""
print_info "Next steps:"
echo "  1. Review the screenshots"
echo "  2. Remove any personal/sensitive information if needed"
echo "  3. Commit and push: git add images/screenshots/ && git commit -m 'Add app screenshots' && git push"

