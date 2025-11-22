# Release Checklist

## 📋 Pre-Release Checklist

### Code Quality
- [ ] All tests pass: `./gradlew test`
- [ ] Code linting passes: `./gradlew lint`
- [ ] No critical bugs in current version
- [ ] All features are working as expected
- [ ] Performance is acceptable
- [ ] Memory usage is optimized

### Version Management
- [ ] Update version in `app/build.gradle.kts`
  - [ ] Increment `versionCode`
  - [ ] Update `versionName` following semantic versioning
- [ ] Update changelog/release notes
- [ ] Update any version references in documentation

### Testing
- [ ] Test on multiple Android devices (different screen sizes)
- [ ] Test on different Android versions (8.0+)
- [ ] Test all app features thoroughly
- [ ] Test offline functionality
- [ ] Test API integrations
- [ ] Test accessibility features
- [ ] Test on low-end devices
- [ ] Test app performance under load

### Security
- [ ] Verify API keys are properly configured
- [ ] Check for any hardcoded sensitive data
- [ ] Ensure keystore is properly configured
- [ ] Verify app signing works correctly

### Documentation
- [ ] Update README.md if needed
- [ ] Update index.html if needed
- [ ] Update any user-facing documentation
- [ ] Prepare release notes

## 🚀 Release Process

### Step 1: Version Bump
```bash
# Bump version (choose one)
./bump-version.sh patch  # For bug fixes
./bump-version.sh minor  # For new features
./bump-version.sh major  # For breaking changes
```

### Step 2: Build and Test
```bash
# Run the enhanced release script
./release_apk_enhanced.sh
```

### Step 3: Manual Testing
- [ ] Install the generated APK on a real device
- [ ] Test all major features
- [ ] Verify the app works correctly
- [ ] Check for any crashes or issues

### Step 4: Git Operations
```bash
# Commit version changes
git add app/build.gradle.kts
git commit -m "Bump version to X.X.X"

# Create a git tag
git tag -a vX.X.X -m "Release vX.X.X"

# Push changes and tags
git push
git push --tags
```

### Step 5: CI/CD Verification
- [ ] Check GitHub Actions build status
- [ ] Verify the automated build completes successfully
- [ ] Download and test the CI-generated APK
- [ ] Ensure the release is created on GitHub

## 📱 Post-Release Checklist

### Release Verification
- [ ] Verify APK is available on GitHub releases
- [ ] Test the final release APK on multiple devices
- [ ] Check that all download links work correctly
- [ ] Verify the landing page is updated

### Monitoring
- [ ] Monitor crash reports (if using crash reporting)
- [ ] Monitor user feedback and reviews
- [ ] Check app performance metrics
- [ ] Monitor API usage and costs

### Communication
- [ ] Announce the release to users
- [ ] Update any external documentation
- [ ] Notify stakeholders about the release
- [ ] Share release notes with users

## 🔧 Troubleshooting

### Common Issues

#### Build Fails
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleRelease
```

#### Keystore Issues
```bash
# Verify keystore exists
ls -la app/release-keystore.jks

# Check keystore validity
keytool -list -v -keystore app/release-keystore.jks
```

#### APK Not Signed
```bash
# Verify APK signing
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

#### Version Conflicts
```bash
# Check current version
grep versionName app/build.gradle.kts
grep versionCode app/build.gradle.kts
```

## 📊 Release Metrics

### Track These Metrics
- [ ] APK size
- [ ] Build time
- [ ] Number of downloads
- [ ] User feedback
- [ ] Crash reports
- [ ] Performance metrics

### Version History
Keep track of:
- Version numbers
- Release dates
- Key changes
- Known issues
- User feedback

## 🎯 Best Practices

### Version Naming
- Use semantic versioning (MAJOR.MINOR.PATCH)
- Increment version code for each release
- Use descriptive commit messages
- Tag releases with meaningful messages

### Testing Strategy
- Test on multiple devices
- Test on different Android versions
- Test edge cases
- Test offline scenarios
- Test performance under load

### Security
- Never commit sensitive data
- Use environment variables for secrets
- Regularly rotate keys
- Keep dependencies updated

### Documentation
- Keep release notes updated
- Document breaking changes
- Maintain changelog
- Update user documentation

## 🚨 Emergency Procedures

### If Release Has Critical Issues
1. **Immediate Actions**
   - [ ] Identify the issue
   - [ ] Create a hotfix branch
   - [ ] Fix the issue
   - [ ] Test thoroughly

2. **Quick Release**
   - [ ] Bump patch version
   - [ ] Build new APK
   - [ ] Test quickly but thoroughly
   - [ ] Release immediately

3. **Communication**
   - [ ] Notify users about the issue
   - [ ] Provide timeline for fix
   - [ ] Update release notes

### Rollback Plan
- [ ] Keep previous version APK
- [ ] Have rollback procedure ready
- [ ] Communicate rollback to users
- [ ] Document lessons learned

---

## 📝 Release Notes Template

```markdown
# Release Notes vX.X.X

## 🎉 What's New
- [List new features]

## 🐛 Bug Fixes
- [List bug fixes]

## 🔧 Improvements
- [List improvements]

## 📱 Technical Details
- **Version:** X.X.X
- **Version Code:** XXX
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **APK Size:** XX MB

## 📋 Installation
1. Download the APK
2. Enable "Install from unknown sources"
3. Install the APK
4. Launch the app

## 🧪 Testing
- [ ] Tested on Android 8.0+
- [ ] Verified all features work
- [ ] Checked for crashes
- [ ] Tested offline functionality

## 🐛 Known Issues
- [List any known issues]

## 📞 Support
For issues or feedback, please contact us at [contact info]
```

---

*This checklist ensures a thorough and reliable release process. Customize it based on your specific needs and requirements.* 