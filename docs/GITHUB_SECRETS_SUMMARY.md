# GitHub Secrets - Complete Setup Guide

## 🎯 What You Need to Do

### Step 1: Add Secrets to GitHub

1. **Go to your repository settings:**
   ```
   https://github.com/gunainvestor/GenAI_life-problem-solver/settings/secrets/actions
   ```

2. **Add these 5 secrets:**

   | Secret Name | Value | Description |
   |-------------|-------|-------------|
   | `RELEASE_KEYSTORE_B64` | `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` | Base64 encoded keystore |
   | `KEYSTORE_PASSWORD` | `lifeproblemsolver2024` | Keystore password |
   | `KEY_ALIAS` | `release-key` | Key alias |
   | `KEY_PASSWORD` | `lifeproblemsolver2024` | Key password |
   | `OPENAI_API_KEY` | `[Your actual OpenAI API key]` | Your OpenAI API key |

### Step 2: Update GitHub Actions Workflow

Your workflow is already configured to use `RELEASE_KEYSTORE_B64`. The enhanced version will use all secrets when you update it.

### Step 3: Test the Setup

1. **Commit your changes:**
   ```bash
   git add .
   git commit -m "Update build.gradle.kts to use environment variables"
   git push
   ```

2. **Trigger a build:**
   - Go to Actions tab in GitHub
   - Click "Build & Release Signed APK"
   - Click "Run workflow"

## 🔧 How It Works

### In GitHub Actions
```yaml
- name: Decrypt release keystore
  env:
    KEYSTORE_B64: ${{ secrets.RELEASE_KEYSTORE_B64 }}
  run: |
    echo "$KEYSTORE_B64" | base64 -d > app/release-keystore.jks

- name: Build signed release APK
  env:
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
    OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
  run: ./gradlew assembleRelease
```

### In build.gradle.kts
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release-keystore.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "lifeproblemsolver2024"
        keyAlias = System.getenv("KEY_ALIAS") ?: "release-key"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "lifeproblemsolver2024"
    }
}
```

## 🛠️ Local Development

### Option 1: Environment Variables
```bash
export KEYSTORE_PASSWORD=lifeproblemsolver2024
export KEY_ALIAS=release-key
export KEY_PASSWORD=lifeproblemsolver2024
export OPENAI_API_KEY=your_actual_api_key_here
```

### Option 2: .env File
Create `.env` file (add to .gitignore):
```bash
KEYSTORE_PASSWORD=lifeproblemsolver2024
KEY_ALIAS=release-key
KEY_PASSWORD=lifeproblemsolver2024
OPENAI_API_KEY=your_actual_api_key_here
```

Then load it:
```bash
export $(cat .env | xargs)
```

## 🔍 Verification

### Check if Secrets Work
```bash
# Test local build with secrets
./gradlew assembleRelease

# Verify APK is signed
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### Check GitHub Actions
1. Go to Actions tab
2. Check the latest workflow run
3. Verify no errors in the build logs

## 🚨 Troubleshooting

### Common Issues

1. **Secret not found in GitHub Actions:**
   - Double-check secret names (case sensitive)
   - Ensure secrets are added to the correct repository

2. **Build fails with signing errors:**
   - Verify keystore base64 is correct
   - Check passwords match the keystore

3. **Local build fails:**
   - Set environment variables
   - Check if keystore file exists

## 📋 Quick Commands

### Generate New Base64 Keystore
```bash
base64 -i app/release-keystore.jks | tr -d '\n' | pbcopy
```

### Test Keystore
```bash
keytool -list -v -keystore app/release-keystore.jks -storepass lifeproblemsolver2024
```

### Check Environment Variables
```bash
env | grep -E "(KEYSTORE|KEY_|OPENAI)"
```

## ✅ Success Checklist

- [ ] Added all 5 secrets to GitHub
- [ ] Updated build.gradle.kts to use environment variables
- [ ] Tested local build with secrets
- [ ] Triggered GitHub Actions build
- [ ] Verified APK is properly signed
- [ ] Confirmed no secrets are committed to repository

---

*Your app is now securely configured to use GitHub Secrets for sensitive data!* 