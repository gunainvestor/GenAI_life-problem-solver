#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="LifeProblemSolver"
BUILD_TYPE="release"

# Function to print colored output
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

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to validate APK
validate_apk() {
    local apk_file="$1"
    
    if [ ! -f "$apk_file" ]; then
        print_error "APK file not found: $apk_file"
        return 1
    fi
    
    # Check if APK is signed
    if command_exists "jarsigner"; then
        if jarsigner -verify -verbose -certs "$apk_file" >/dev/null 2>&1; then
            print_status "APK is properly signed"
        else
            print_warning "APK signing verification failed"
        fi
    fi
    
    # Show APK info
    local apk_size=$(ls -lh "$apk_file" | awk '{print $5}')
    print_info "APK Size: $apk_size"
    
    return 0
}

echo -e "${GREEN}🚀 Starting enhanced automated release process...${NC}"

# Check prerequisites
print_info "Checking prerequisites..."

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    print_error "Not in Android project root directory"
    exit 1
fi

# Check if keystore exists
if [ ! -f "app/release-keystore.jks" ]; then
    print_error "release-keystore.jks not found! Please ensure your keystore is in app/release-keystore.jks"
    exit 1
fi

# Check if gradlew is executable
if [ ! -x "./gradlew" ]; then
    print_error "gradlew is not executable. Run: chmod +x gradlew"
    exit 1
fi

print_status "Prerequisites check passed"

# Clean previous builds
print_info "Cleaning previous builds..."
./gradlew clean

# Build signed release APK
print_info "Building signed release APK..."
if ./gradlew assembleRelease; then
    print_status "Build completed successfully"
else
    print_error "Build failed"
    exit 1
fi

# Get version info
VERSION_NAME=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
VERSION_CODE=$(grep versionCode app/build.gradle.kts | head -1 | cut -d'=' -f2 | tr -d ' ')
APK_NAME="${APP_NAME}-v${VERSION_NAME}-release.apk"

print_info "Version Name: $VERSION_NAME"
print_info "Version Code: $VERSION_CODE"

# Copy and rename the APK
if cp app/build/outputs/apk/release/app-release.apk "$APK_NAME"; then
    print_status "APK copied and renamed: $APK_NAME"
else
    print_error "Failed to copy APK"
    exit 1
fi

# Validate the APK
print_info "Validating APK..."
if validate_apk "$APK_NAME"; then
    print_status "APK validation passed"
else
    print_warning "APK validation had issues"
fi

# Update documentation
print_info "Updating documentation..."

# Backup original files
cp README.md README.md.backup 2>/dev/null || true
cp index.html index.html.backup 2>/dev/null || true

# Update README.md
if sed -i '' "s/${APP_NAME}-v[0-9.]*-release.apk/$APK_NAME/g" README.md; then
    print_status "Updated README.md"
else
    print_warning "Failed to update README.md"
fi

# Update index.html
if sed -i '' "s/href=\"${APP_NAME}-v[0-9.]*-release.apk\"/href=\"$APK_NAME\"/g" index.html; then
    print_status "Updated index.html download link"
else
    print_warning "Failed to update index.html download link"
fi

if sed -i '' "s/Download APK v[0-9.]*/Download APK v${VERSION_NAME}/g" index.html; then
    print_status "Updated index.html version text"
else
    print_warning "Failed to update index.html version text"
fi

# Create release notes
RELEASE_NOTES_FILE="RELEASE_NOTES_v${VERSION_NAME}.md"
print_info "Creating release notes: $RELEASE_NOTES_FILE"

cat > "$RELEASE_NOTES_FILE" << EOF
# Release Notes v${VERSION_NAME}

## Version Information
- **Version Name:** ${VERSION_NAME}
- **Version Code:** ${VERSION_CODE}
- **Build Type:** ${BUILD_TYPE}
- **APK Size:** $(ls -lh "$APK_NAME" | awk '{print $5}')
- **Build Date:** $(date)

## Changes
- [Add your changes here]

## Installation
Download the APK from the releases page and install on your Android device.

## Testing Checklist
- [ ] Test on Android 8.0+ devices
- [ ] Verify all features work correctly
- [ ] Check for any crashes or issues
- [ ] Test offline functionality
- [ ] Verify API integrations
- [ ] Test on different screen sizes
- [ ] Verify accessibility features

## Known Issues
- [List any known issues]

## Technical Details
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **Build Tools:** Latest

---
*Generated on $(date)*
EOF

print_status "Created release notes: $RELEASE_NOTES_FILE"

# Show git status
print_info "Git status:"
git status --porcelain | head -10

# Show APK info
APK_SIZE=$(ls -lh "$APK_NAME" | awk '{print $5}')
print_info "Final APK Size: $APK_SIZE"

echo ""
echo -e "${GREEN}🎯 Release v${VERSION_NAME} is ready!${NC}"
echo ""
echo -e "${YELLOW}👉 Next steps:${NC}"
echo "   1. Test $APK_NAME on a real device"
echo "   2. Review and update $RELEASE_NOTES_FILE"
echo "   3. Commit changes:"
echo "      git add . && git commit -m \"Release v${VERSION_NAME}\""
echo "   4. Create a tag:"
echo "      git tag -a v${VERSION_NAME} -m \"Release v${VERSION_NAME}\""
echo "   5. Push changes:"
echo "      git push && git push --tags"
echo ""
echo -e "${GREEN}📱 The APK will be available at:${NC}"
echo "   https://github.com/gunainvestor/GenAI_life-problem-solver/releases"
echo ""
echo -e "${BLUE}📊 Build Summary:${NC}"
echo "   - APK: $APK_NAME"
echo "   - Size: $APK_SIZE"
echo "   - Version: $VERSION_NAME ($VERSION_CODE)"
echo "   - Build Type: $BUILD_TYPE"
echo ""
print_status "Release process completed successfully!" 