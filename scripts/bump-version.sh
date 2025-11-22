#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

VERSION_TYPE=$1  # major, minor, patch

if [ -z "$VERSION_TYPE" ]; then
    echo "Usage: ./bump-version.sh [major|minor|patch]"
    echo ""
    echo "Examples:"
    echo "  ./bump-version.sh patch  # 1.6.0 -> 1.6.1"
    echo "  ./bump-version.sh minor  # 1.6.0 -> 1.7.0"
    echo "  ./bump-version.sh major  # 1.6.0 -> 2.0.0"
    exit 1
fi

# Validate version type
case $VERSION_TYPE in
    major|minor|patch)
        ;;
    *)
        print_error "Invalid version type: $VERSION_TYPE"
        echo "Valid types: major, minor, patch"
        exit 1
        ;;
esac

# Check if build.gradle.kts exists
if [ ! -f "app/build.gradle.kts" ]; then
    print_error "app/build.gradle.kts not found"
    exit 1
fi

# Read current version
CURRENT_VERSION=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
CURRENT_VERSION_CODE=$(grep versionCode app/build.gradle.kts | head -1 | cut -d'=' -f2 | tr -d ' ')

if [ -z "$CURRENT_VERSION" ] || [ -z "$CURRENT_VERSION_CODE" ]; then
    print_error "Could not read current version from build.gradle.kts"
    exit 1
fi

print_info "Current Version: $CURRENT_VERSION (Code: $CURRENT_VERSION_CODE)"

# Parse version components
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]:-0}  # Default to 0 if patch is not specified

# Bump version based on type
case $VERSION_TYPE in
    major)
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
        ;;
    minor)
        MINOR=$((MINOR + 1))
        PATCH=0
        ;;
    patch)
        PATCH=$((PATCH + 1))
        ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

print_info "New Version: $NEW_VERSION (Code: $NEW_VERSION_CODE)"

# Backup build.gradle.kts
cp app/build.gradle.kts app/build.gradle.kts.backup

# Update build.gradle.kts
if sed -i '' "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/g" app/build.gradle.kts; then
    print_status "Updated versionCode: $CURRENT_VERSION_CODE -> $NEW_VERSION_CODE"
else
    print_error "Failed to update versionCode"
    exit 1
fi

if sed -i '' "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/g" app/build.gradle.kts; then
    print_status "Updated versionName: $CURRENT_VERSION -> $NEW_VERSION"
else
    print_error "Failed to update versionName"
    exit 1
fi

# Verify changes
VERIFIED_VERSION=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
VERIFIED_VERSION_CODE=$(grep versionCode app/build.gradle.kts | head -1 | cut -d'=' -f2 | tr -d ' ')

if [ "$VERIFIED_VERSION" = "$NEW_VERSION" ] && [ "$VERIFIED_VERSION_CODE" = "$NEW_VERSION_CODE" ]; then
    print_status "Version bump successful!"
    echo ""
    echo -e "${GREEN}📊 Version Summary:${NC}"
    echo "   - Version Name: $CURRENT_VERSION → $NEW_VERSION"
    echo "   - Version Code: $CURRENT_VERSION_CODE → $NEW_VERSION_CODE"
    echo "   - Bump Type: $VERSION_TYPE"
    echo ""
    echo -e "${YELLOW}👉 Next steps:${NC}"
    echo "   1. Review the changes: git diff app/build.gradle.kts"
    echo "   2. Commit the version bump:"
    echo "      git add app/build.gradle.kts && git commit -m \"Bump version to $NEW_VERSION\""
    echo "   3. Run release script: ./release_apk_enhanced.sh"
else
    print_error "Version bump verification failed"
    print_error "Expected: $NEW_VERSION, Got: $VERIFIED_VERSION"
    print_error "Expected Code: $NEW_VERSION_CODE, Got: $VERIFIED_VERSION_CODE"
    
    # Restore backup
    cp app/build.gradle.kts.backup app/build.gradle.kts
    print_warning "Restored original build.gradle.kts"
    exit 1
fi 