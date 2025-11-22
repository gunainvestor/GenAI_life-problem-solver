#!/bin/bash

# Script to update version in all places:
# 1. app/build.gradle.kts (versionCode and versionName)
# 2. index.html (landing page version and APK link)
# Note: SettingsScreen.kt now uses BuildConfig.VERSION_NAME automatically

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Check if version is provided
if [ -z "$1" ]; then
    print_error "Version not provided"
    echo "Usage: ./scripts/update-version.sh <version> [versionCode]"
    echo ""
    echo "Examples:"
    echo "  ./scripts/update-version.sh 1.6.5"
    echo "  ./scripts/update-version.sh 1.6.5 11"
    echo ""
    echo "If versionCode is not provided, it will be auto-incremented"
    exit 1
fi

NEW_VERSION="$1"
NEW_VERSION_CODE="$2"

# Validate version format (x.y.z)
if ! [[ "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    print_error "Invalid version format: $NEW_VERSION"
    echo "Version must be in format: x.y.z (e.g., 1.6.5)"
    exit 1
fi

# Check if files exist
if [ ! -f "app/build.gradle.kts" ]; then
    print_error "app/build.gradle.kts not found"
    exit 1
fi

if [ ! -f "index.html" ]; then
    print_error "index.html not found"
    exit 1
fi

# Read current version
CURRENT_VERSION=$(grep 'versionName =' app/build.gradle.kts | head -1 | cut -d'"' -f2)
CURRENT_VERSION_CODE=$(grep 'versionCode =' app/build.gradle.kts | head -1 | cut -d'=' -f2 | tr -d ' ')

if [ -z "$CURRENT_VERSION" ] || [ -z "$CURRENT_VERSION_CODE" ]; then
    print_error "Could not read current version from build.gradle.kts"
    exit 1
fi

print_info "Current Version: $CURRENT_VERSION (Code: $CURRENT_VERSION_CODE)"
print_info "New Version: $NEW_VERSION"

# Auto-increment versionCode if not provided
if [ -z "$NEW_VERSION_CODE" ]; then
    NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
    print_info "Auto-incremented Version Code: $NEW_VERSION_CODE"
else
    # Validate versionCode is a number
    if ! [[ "$NEW_VERSION_CODE" =~ ^[0-9]+$ ]]; then
        print_error "Invalid versionCode: $NEW_VERSION_CODE (must be a number)"
        exit 1
    fi
fi

# Update app/build.gradle.kts
print_info "Updating app/build.gradle.kts..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" app/build.gradle.kts
    sed -i '' "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts
else
    # Linux
    sed -i "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" app/build.gradle.kts
    sed -i "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts
fi

if grep -q "versionName = \"$NEW_VERSION\"" app/build.gradle.kts && grep -q "versionCode = $NEW_VERSION_CODE" app/build.gradle.kts; then
    print_status "Updated app/build.gradle.kts"
else
    print_error "Failed to update app/build.gradle.kts"
    exit 1
fi

# Update index.html - version display
print_info "Updating index.html version display..."
APK_NAME="LifeProblemSolver-v${NEW_VERSION}-release.apk"
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/Life Problem Solver v[0-9]\+\.[0-9]\+\.[0-9]\+/Life Problem Solver v${NEW_VERSION}/g" index.html
    sed -i '' "s/href=\"releases\/LifeProblemSolver-v[0-9]\+\.[0-9]\+\.[0-9]\+-release\.apk\"/href=\"releases\/${APK_NAME}\"/g" index.html
else
    # Linux
    sed -i "s/Life Problem Solver v[0-9]\+\.[0-9]\+\.[0-9]\+/Life Problem Solver v${NEW_VERSION}/g" index.html
    sed -i "s/href=\"releases\/LifeProblemSolver-v[0-9]\+\.[0-9]\+\.[0-9]\+-release\.apk\"/href=\"releases\/${APK_NAME}\"/g" index.html
fi

if grep -q "Life Problem Solver v${NEW_VERSION}" index.html && grep -q "href=\"releases/${APK_NAME}\"" index.html; then
    print_status "Updated index.html"
else
    print_warning "index.html update may have issues - please verify manually"
fi

# Summary
echo ""
print_status "Version update complete!"
echo ""
echo "Updated files:"
echo "  ✓ app/build.gradle.kts"
echo "  ✓ index.html"
echo ""
echo "Version: $NEW_VERSION (Code: $NEW_VERSION_CODE)"
echo "APK Name: $APK_NAME"
echo ""
print_info "Next steps:"
echo "  1. Build the APK: ./gradlew assembleRelease"
echo "  2. Copy APK: cp app/build/outputs/apk/release/app-release.apk releases/${APK_NAME}"
echo "  3. Commit changes: git add -A && git commit -m \"chore: update to version ${NEW_VERSION}\""

