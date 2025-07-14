#!/bin/bash

set -e

echo "🚀 Starting automated release process..."

# 1. Build signed release APK
echo "📱 Building signed release APK..."
./gradlew assembleRelease

# 2. Get version name from build.gradle.kts
VERSION_NAME=$(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)
APK_NAME="LifeProblemSolver-v${VERSION_NAME}-release.apk"

# 3. Copy and rename the APK
cp app/build/outputs/apk/release/app-release.apk "$APK_NAME"
echo "✅ Built and signed APK: $APK_NAME"

# 4. Update README.md download link
echo "📝 Updating README.md..."
# Update the main download link
sed -i '' "s/LifeProblemSolver-v[0-9.]*-release.apk/$APK_NAME/g" README.md
# Update the final download link at the bottom
sed -i '' "s/LifeProblemSolver-Tag6.apk/$APK_NAME/g" README.md

# 5. Update index.html download section
echo "🌐 Updating index.html..."
sed -i '' "s/href=\"LifeProblemSolver-v[0-9.]*-release.apk\"/href=\"$APK_NAME\"/g" index.html
sed -i '' "s/Download APK v[0-9.]*/Download APK v${VERSION_NAME}/g" index.html
sed -i '' "s/custom_parameter_1': 'LifeProblemSolver-v[0-9.]*-release.apk'/custom_parameter_1': '$APK_NAME'/g" index.html

echo "✅ Updated README.md and index.html download links."

# 6. Show APK file size
APK_SIZE=$(ls -lh "$APK_NAME" | awk '{print $5}')
echo "📊 APK Size: $APK_SIZE"

# 7. Remind to test and push
echo ""
echo "🎯 Release v${VERSION_NAME} is ready!"
echo ""
echo "👉 Next steps:"
echo "   1. Test $APK_NAME on a real device"
echo "   2. If everything works, run:"
echo "      git add . && git commit -m \"Release v${VERSION_NAME}\" && git push"
echo ""
echo "📱 The APK will be available at:"
echo "   https://github.com/gunainvestor/GenAI_life-problem-solver/blob/main/$APK_NAME"
echo ""
echo "🌐 Your landing page will be updated at:"
echo "   https://gunainvestor.github.io/GenAI_life-problem-solver" 