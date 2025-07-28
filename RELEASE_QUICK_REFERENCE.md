# Release Quick Reference

## 🚀 Quick Release Commands

### 1. Bump Version
```bash
# For bug fixes
./bump-version.sh patch

# For new features
./bump-version.sh minor

# For breaking changes
./bump-version.sh major
```

### 2. Build Release APK
```bash
# Enhanced release script (recommended)
./release_apk_enhanced.sh

# Or manual build
./gradlew assembleRelease
```

### 3. Git Operations
```bash
# Commit version changes
git add app/build.gradle.kts
git commit -m "Bump version to X.X.X"

# Create tag
git tag -a vX.X.X -m "Release vX.X.X"

# Push everything
git push && git push --tags
```

## 📱 Common Tasks

### Check Current Version
```bash
grep versionName app/build.gradle.kts
grep versionCode app/build.gradle.kts
```

### Verify APK Signing
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### Clean Build
```bash
./gradlew clean
./gradlew assembleRelease
```

### Check APK Size
```bash
ls -lh app/build/outputs/apk/release/app-release.apk
```

## 🔧 Troubleshooting

### Build Issues
```bash
# Clean everything
./gradlew clean
rm -rf .gradle
rm -rf app/build

# Rebuild
./gradlew assembleRelease
```

### Keystore Issues
```bash
# Check keystore
ls -la app/release-keystore.jks

# Verify keystore
keytool -list -v -keystore app/release-keystore.jks
```

### Version Issues
```bash
# Check all version references
grep -r "version" app/build.gradle.kts
```

## 📊 Release Workflow

### Standard Release Process
1. **Bump Version**: `./bump-version.sh patch`
2. **Build APK**: `./release_apk_enhanced.sh`
3. **Test APK**: Install on device and test
4. **Commit**: `git add . && git commit -m "Release vX.X.X"`
5. **Tag**: `git tag -a vX.X.X -m "Release vX.X.X"`
6. **Push**: `git push && git push --tags`
7. **Verify**: Check GitHub Actions and releases

### Hotfix Process
1. **Create Hotfix Branch**: `git checkout -b hotfix/vX.X.X`
2. **Fix Issue**: Make necessary changes
3. **Bump Patch**: `./bump-version.sh patch`
4. **Build**: `./release_apk_enhanced.sh`
5. **Test**: Quick but thorough testing
6. **Release**: Follow standard process
7. **Merge**: `git checkout main && git merge hotfix/vX.X.X`

## 🔒 Security Commands

### Check for Hardcoded Secrets
```bash
# Search for potential API keys
grep -r "sk-" .
grep -r "AIza" .
grep -r "password" .
```

### Verify Environment Variables
```bash
# Check if API keys are properly configured
echo $OPENAI_API_KEY
```

## 📋 Pre-Release Checklist (Quick)

- [ ] Tests pass: `./gradlew test`
- [ ] Lint passes: `./gradlew lint`
- [ ] Version updated: `./bump-version.sh patch`
- [ ] APK built: `./release_apk_enhanced.sh`
- [ ] APK tested on device
- [ ] Git committed and tagged
- [ ] GitHub Actions passed

## 🎯 Post-Release Checklist (Quick)

- [ ] APK available on GitHub releases
- [ ] Download links work
- [ ] Landing page updated
- [ ] Release notes published
- [ ] Monitor for issues

## 📱 APK Information

### Current App Details
- **Package**: `com.lifeproblemsolver.app`
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Current Version**: 1.6
- **Current Version Code**: 6

### APK Location
- **Built APK**: `app/build/outputs/apk/release/app-release.apk`
- **Renamed APK**: `LifeProblemSolver-vX.X.X-release.apk`

## 🔗 Useful Links

- **GitHub Releases**: https://github.com/gunainvestor/GenAI_life-problem-solver/releases
- **Landing Page**: https://gunainvestor.github.io/GenAI_life-problem-solver
- **GitHub Actions**: https://github.com/gunainvestor/GenAI_life-problem-solver/actions

## 📞 Emergency Contacts

- **Repository**: https://github.com/gunainvestor/GenAI_life-problem-solver
- **Issues**: https://github.com/gunainvestor/GenAI_life-problem-solver/issues

---

*Keep this reference handy for quick access to common release tasks and commands.* 