# GitHub Secrets Setup Guide

## 🔐 What are GitHub Secrets?

GitHub Secrets are encrypted environment variables that you can use in your GitHub Actions workflows. They're perfect for storing sensitive data like:
- API keys
- Keystore passwords
- Signing certificates
- Database credentials
- Any other sensitive configuration

## 📋 Setting Up Secrets

### Step 1: Access Repository Settings

1. Go to your GitHub repository: `https://github.com/gunainvestor/GenAI_life-problem-solver`
2. Click on **Settings** tab
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret**

### Step 2: Add Required Secrets

#### 1. **RELEASE_KEYSTORE_B64** (Required for CI/CD)
This is your keystore file encoded in base64.

**How to create:**
```bash
# Encode your keystore to base64
base64 -i app/release-keystore.jks | tr -d '\n'
```

**Add to GitHub:**
- **Name**: `RELEASE_KEYSTORE_B64`
- **Value**: The base64 encoded keystore content
- **Description**: Base64 encoded release keystore for signing APKs

#### 2. **KEYSTORE_PASSWORD** (Optional - for enhanced security)
Your keystore password.

**Add to GitHub:**
- **Name**: `KEYSTORE_PASSWORD`
- **Value**: `lifeproblemsolver2024`
- **Description**: Password for the release keystore

#### 3. **KEY_ALIAS** (Optional - for enhanced security)
Your key alias.

**Add to GitHub:**
- **Name**: `KEY_ALIAS`
- **Value**: `release-key`
- **Description**: Alias for the release key

#### 4. **KEY_PASSWORD** (Optional - for enhanced security)
Your key password.

**Add to GitHub:**
- **Name**: `KEY_PASSWORD`
- **Value**: `lifeproblemsolver2024`
- **Description**: Password for the release key

#### 5. **OPENAI_API_KEY** (For API access)
Your OpenAI API key.

**Add to GitHub:**
- **Name**: `OPENAI_API_KEY`
- **Value**: Your actual OpenAI API key
- **Description**: OpenAI API key for the app

## 🔧 Using Secrets in GitHub Actions

### Current Workflow (Already Configured)
Your `.github/workflows/release-apk.yml` already uses `RELEASE_KEYSTORE_B64`:

```yaml
- name: Decrypt release keystore
  env:
    KEYSTORE_B64: ${{ secrets.RELEASE_KEYSTORE_B64 }}
  run: |
    echo "$KEYSTORE_B64" | base64 -d > app/release-keystore.jks
```

### Enhanced Workflow with All Secrets
Here's an improved version that uses all secrets:

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
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
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
```

## 🔄 Updating build.gradle.kts to Use Secrets

### Option 1: Environment Variables (Recommended)
Update your `app/build.gradle.kts` to use environment variables:

```kotlin
android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            storeFile = file("release-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "lifeproblemsolver2024"
            keyAlias = System.getenv("KEY_ALIAS") ?: "release-key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "lifeproblemsolver2024"
        }
    }

    defaultConfig {
        // ... existing config ...
        
        // Use environment variable for API key
        buildConfigField("String", "OPENAI_API_KEY", "\"${System.getenv("OPENAI_API_KEY") ?: ""}\"")
    }
}
```

### Option 2: Gradle Properties (Alternative)
Create a `gradle.properties` file with placeholders:

```properties
# Gradle properties for secrets
KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD}
KEY_ALIAS=${KEY_ALIAS}
KEY_PASSWORD=${KEY_PASSWORD}
OPENAI_API_KEY=${OPENAI_API_KEY}
```

Then in `build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release-keystore.jks")
        storePassword = project.findProperty("KEYSTORE_PASSWORD") as String?
        keyAlias = project.findProperty("KEY_ALIAS") as String?
        keyPassword = project.findProperty("KEY_PASSWORD") as String?
    }
}
```

## 🛠️ Local Development with Secrets

### Option 1: Environment Variables
Create a `.env` file (add to .gitignore):
```bash
# .env file
KEYSTORE_PASSWORD=lifeproblemsolver2024
KEY_ALIAS=release-key
KEY_PASSWORD=lifeproblemsolver2024
OPENAI_API_KEY=your_openai_api_key_here
```

Load it in your shell:
```bash
export $(cat .env | xargs)
```

### Option 2: Direct Export
```bash
export KEYSTORE_PASSWORD=lifeproblemsolver2024
export KEY_ALIAS=release-key
export KEY_PASSWORD=lifeproblemsolver2024
export OPENAI_API_KEY=your_openai_api_key_here
```

## 🔍 Verifying Secrets

### Check if Secrets are Set
```bash
# In GitHub Actions
echo "KEYSTORE_PASSWORD is set: ${{ secrets.KEYSTORE_PASSWORD != '' }}"

# Locally
echo "KEYSTORE_PASSWORD: ${KEYSTORE_PASSWORD:+SET}"
```

### Test Keystore Decryption
```bash
# Test the base64 decoding
echo "$KEYSTORE_B64" | base64 -d > test-keystore.jks
ls -la test-keystore.jks
```

## 🔒 Security Best Practices

### 1. **Never Commit Secrets**
- Add sensitive files to `.gitignore`
- Use environment variables
- Use GitHub Secrets for CI/CD

### 2. **Rotate Secrets Regularly**
- Change keystore passwords periodically
- Rotate API keys
- Update secrets in GitHub

### 3. **Limit Access**
- Only repository owners should manage secrets
- Use organization secrets for shared projects
- Audit secret access regularly

### 4. **Monitor Usage**
- Check GitHub Actions logs for secret usage
- Monitor for unauthorized access
- Review secret permissions

## 🚨 Troubleshooting

### Common Issues

#### 1. **Secret Not Found**
```yaml
# Check if secret exists
- name: Debug secrets
  run: |
    if [ -n "${{ secrets.KEYSTORE_PASSWORD }}" ]; then
      echo "Secret is set"
    else
      echo "Secret is not set"
    fi
```

#### 2. **Base64 Decoding Fails**
```bash
# Verify base64 content
echo "$KEYSTORE_B64" | base64 -d | file -
```

#### 3. **Build Fails with Secrets**
```yaml
# Add debug step
- name: Debug environment
  run: |
    echo "KEYSTORE_PASSWORD length: ${#KEYSTORE_PASSWORD}"
    echo "KEY_ALIAS: $KEY_ALIAS"
    echo "OPENAI_API_KEY length: ${#OPENAI_API_KEY}"
```

## 📋 Secret Management Checklist

### Setup
- [ ] Create all required secrets in GitHub
- [ ] Update build.gradle.kts to use environment variables
- [ ] Test local development with secrets
- [ ] Verify CI/CD workflow works
- [ ] Document secret usage

### Maintenance
- [ ] Rotate secrets periodically
- [ ] Update documentation when secrets change
- [ ] Monitor secret usage
- [ ] Review access permissions

### Security
- [ ] Never commit secrets to repository
- [ ] Use strong passwords for keystores
- [ ] Limit access to secrets
- [ ] Monitor for unauthorized access

## 🔗 Useful Commands

### Generate Base64 Keystore
```bash
# Encode keystore
base64 -i app/release-keystore.jks | tr -d '\n' | pbcopy

# Decode keystore (for testing)
echo "YOUR_BASE64_CONTENT" | base64 -d > test-keystore.jks
```

### Check Secret Values (Local)
```bash
# Check if environment variables are set
env | grep -E "(KEYSTORE|KEY_|OPENAI)"
```

### Test Keystore
```bash
# Verify keystore with password
keytool -list -v -keystore app/release-keystore.jks -storepass lifeproblemsolver2024
```

---

*This guide ensures your sensitive data is properly secured while maintaining functionality in both local development and CI/CD environments.* 