# Android App Signing & Release Automation Guide

## 🔐 App Signing Configuration

### Current Setup
Your app is configured with a release keystore in `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release-keystore.jks")
        storePassword = "lifeproblemsolver2024"
        keyAlias = "release-key"
        keyPassword = "lifeproblemsolver2024"
    }
}
```

### Security Best Practices

#### Option 1: Environment Variables (Recommended)
Move sensitive data to environment variables:

```kotlin
// In app/build.gradle.kts
signingConfigs {
    create("release") {
        storeFile = file("release-keystore.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "lifeproblemsolver2024"
        keyAlias = System.getenv("KEY_ALIAS") ?: "release-key"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "lifeproblemsolver2024"
    }
}
```

#### Option 2: Gradle Properties
Add to `gradle.properties`:
```properties
KEYSTORE_PASSWORD=lifeproblemsolver2024
KEY_ALIAS=release-key
KEY_PASSWORD=lifeproblemsolver2024
```

Then in `build.gradle.kts`:
```kotlin
storePassword = project.findProperty("KEYSTORE_PASSWORD") as String?
keyAlias = project.findProperty("KEY_ALIAS") as String?
keyPassword = project.findProperty("KEY_PASSWORD") as String?
```

## 🚀 Release Automation

### Local Release Process

#### 1. Manual Release Script
Your current `release_apk.sh` script:
```bash
#!/bin/bash
set -e

echo "🚀 Starting automated release process..."

# Build signed release APK
./gradlew assembleRelease

# Get version name
VERSION_NAME=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
APK_NAME="LifeProblemSolver-v${VERSION_NAME}-release.apk"

# Copy and rename APK
cp app/build/outputs/apk/release/app-release.apk "$APK_NAME"

# Update documentation
sed -i '' "s/LifeProblemSolver-v[0-9.]*-release.apk/$APK_NAME/g" README.md
sed -i '' "s/href=\"LifeProblemSolver-v[0-9.]*-release.apk\"/href=\"$APK_NAME\"/g" index.html
```

#### 2. Enhanced Release Script
Create an improved version with better error handling:

```bash
#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="LifeProblemSolver"
BUILD_TYPE="release"

echo -e "${GREEN}🚀 Starting automated release process...${NC}"

# Check if keystore exists
if [ ! -f "app/release-keystore.jks" ]; then
    echo -e "${RED}❌ Error: release-keystore.jks not found!${NC}"
    exit 1
fi

# Clean previous builds
echo -e "${YELLOW}🧹 Cleaning previous builds...${NC}"
./gradlew clean

# Build signed release APK
echo -e "${YELLOW}📱 Building signed release APK...${NC}"
./gradlew assembleRelease

# Get version info
VERSION_NAME=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
VERSION_CODE=$(grep versionCode app/build.gradle.kts | head -1 | cut -d'=' -f2 | tr -d ' ')
APK_NAME="${APP_NAME}-v${VERSION_NAME}-release.apk"

# Copy and rename the APK
cp app/build/outputs/apk/release/app-release.apk "$APK_NAME"
echo -e "${GREEN}✅ Built and signed APK: $APK_NAME${NC}"

# Show APK info
APK_SIZE=$(ls -lh "$APK_NAME" | awk '{print $5}')
echo -e "${GREEN}📊 APK Size: $APK_SIZE${NC}"

# Update documentation
echo -e "${YELLOW}📝 Updating documentation...${NC}"
sed -i '' "s/${APP_NAME}-v[0-9.]*-release.apk/$APK_NAME/g" README.md
sed -i '' "s/href=\"${APP_NAME}-v[0-9.]*-release.apk\"/href=\"$APK_NAME\"/g" index.html
sed -i '' "s/Download APK v[0-9.]*/Download APK v${VERSION_NAME}/g" index.html

echo -e "${GREEN}✅ Updated README.md and index.html${NC}"

# Create release notes
RELEASE_NOTES_FILE="RELEASE_NOTES_v${VERSION_NAME}.md"
cat > "$RELEASE_NOTES_FILE" << EOF
# Release Notes v${VERSION_NAME}

## Version Information
- **Version Name:** ${VERSION_NAME}
- **Version Code:** ${VERSION_CODE}
- **Build Type:** ${BUILD_TYPE}
- **APK Size:** ${APK_SIZE}

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

## Known Issues
- [List any known issues]

---
*Generated on $(date)*
EOF

echo -e "${GREEN}✅ Created release notes: $RELEASE_NOTES_FILE${NC}"

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
```

### CI/CD Pipeline

#### GitHub Actions Workflow
Your current `.github/workflows/release-apk.yml` is well-configured. Here's an enhanced version:

```yaml
name: Build & Release Signed APK

on:
  workflow_dispatch:
  push:
    branches: [main]
    paths:
      - 'app/build.gradle.kts'
      - 'app/src/**'
      - 'gradle/**'
  release:
    types: [published]

jobs:
  build-release:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: 17

      - name: Decrypt release keystore
        env:
          KEYSTORE_B64: ${{ secrets.RELEASE_KEYSTORE_B64 }}
        run: |
          echo "$KEYSTORE_B64" | base64 -d > app/release-keystore.jks

      - name: Set up Gradle
        uses: gradle/gradle-build-action@v3

      - name: Build signed release APK
        run: ./gradlew assembleRelease

      - name: Get version info
        id: version
        run: |
          VERSION_NAME=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
          VERSION_CODE=$(grep versionCode app/build.gradle.kts | head -1 | cut -d'=' -f2 | tr -d ' ')
          echo "version=$VERSION_NAME" >> $GITHUB_OUTPUT
          echo "version_code=$VERSION_CODE" >> $GITHUB_OUTPUT

      - name: Rename APK with version
        run: |
          cp app/build/outputs/apk/release/app-release.apk LifeProblemSolver-v${{ steps.version.outputs.version }}-release.apk

      - name: Upload APK as artifact
        uses: actions/upload-artifact@v4
        with:
          name: LifeProblemSolver-APK-v${{ steps.version.outputs.version }}
          path: LifeProblemSolver-v${{ steps.version.outputs.version }}-release.apk
          retention-days: 30

      - name: Create Release
        uses: softprops/action-gh-release@v2
        if: startsWith(github.ref, 'refs/tags/')
        with:
          files: LifeProblemSolver-v${{ steps.version.outputs.version }}-release.apk
          generate_release_notes: true
          draft: false
          prerelease: false
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

      - name: Comment on PR
        if: github.event_name == 'pull_request'
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: `✅ **APK Build Complete!**
              
              **Version:** ${{ steps.version.outputs.version }}
              **Version Code:** ${{ steps.version.outputs.version_code }}
              **APK:** LifeProblemSolver-v${{ steps.version.outputs.version }}-release.apk
              
              Download the APK from the Actions artifacts tab above.`
            })
```

## 🔧 Version Management

### Semantic Versioning
Follow semantic versioning (MAJOR.MINOR.PATCH):

```kotlin
// In app/build.gradle.kts
defaultConfig {
    versionCode = 6        // Increment for each release
    versionName = "1.6"    // Semantic version
}
```

### Automated Version Bumping
Create a version bump script:

```bash
#!/bin/bash
# bump-version.sh

VERSION_TYPE=$1  # major, minor, patch

if [ -z "$VERSION_TYPE" ]; then
    echo "Usage: ./bump-version.sh [major|minor|patch]"
    exit 1
fi

# Read current version
CURRENT_VERSION=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
CURRENT_VERSION_CODE=$(grep versionCode app/build.gradle.kts | head -1 | cut -d'=' -f2 | tr -d ' ')

# Parse version components
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]}

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
    *)
        echo "Invalid version type: $VERSION_TYPE"
        exit 1
        ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

# Update build.gradle.kts
sed -i '' "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/g" app/build.gradle.kts
sed -i '' "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/g" app/build.gradle.kts

echo "✅ Bumped version from $CURRENT_VERSION to $NEW_VERSION"
echo "✅ Bumped version code from $CURRENT_VERSION_CODE to $NEW_VERSION_CODE"
```

## 📋 Release Checklist

### Pre-Release
- [ ] Update version in `build.gradle.kts`
- [ ] Update changelog/release notes
- [ ] Test on multiple devices
- [ ] Verify all features work
- [ ] Check for memory leaks
- [ ] Test offline functionality

### Release Process
- [ ] Run release script: `./release_apk.sh`
- [ ] Test the generated APK
- [ ] Create git tag: `git tag -a v1.6 -m "Release v1.6"`
- [ ] Push changes: `git push && git push --tags`
- [ ] Verify GitHub Actions build
- [ ] Download and test the final APK

### Post-Release
- [ ] Update documentation
- [ ] Announce release
- [ ] Monitor crash reports
- [ ] Gather user feedback

## 🔒 Security Considerations

### Keystore Management
1. **Backup your keystore**: Store it securely
2. **Use environment variables**: Don't hardcode passwords
3. **Rotate keys periodically**: For security
4. **Use different keys**: For debug vs release

### GitHub Secrets
Set up these secrets in your GitHub repository:
- `RELEASE_KEYSTORE_B64`: Base64 encoded keystore
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

## 🛠️ Troubleshooting

### Common Issues

1. **Keystore not found**
   ```bash
   # Ensure keystore exists
   ls -la app/release-keystore.jks
   ```

2. **Build fails**
   ```bash
   # Clean and rebuild
   ./gradlew clean
   ./gradlew assembleRelease
   ```

3. **APK not signed**
   ```bash
   # Verify signing
   jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
   ```

4. **Version conflicts**
   ```bash
   # Check current version
   grep versionName app/build.gradle.kts
   grep versionCode app/build.gradle.kts
   ```

## 📚 Additional Resources

- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [GitHub Actions for Android](https://docs.github.com/en/actions/guides/building-and-testing-java-with-gradle)
- [Semantic Versioning](https://semver.org/)
- [Android Release Best Practices](https://developer.android.com/distribute/best-practices/launch)

---

*This guide covers the essential aspects of Android app signing and release automation. Your current setup is already well-configured, but these enhancements will make your release process more robust and secure.* 