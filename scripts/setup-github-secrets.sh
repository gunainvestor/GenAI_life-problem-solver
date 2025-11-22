#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔐 GitHub Secrets Setup Helper${NC}"
echo ""

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

# Check if keystore exists
if [ ! -f "app/release-keystore.jks" ]; then
    print_error "Keystore not found at app/release-keystore.jks"
    exit 1
fi

print_info "Generating GitHub Secrets values..."

# Generate base64 keystore
KEYSTORE_B64=$(base64 -i app/release-keystore.jks | tr -d '\n')

print_status "Generated base64 keystore (length: ${#KEYSTORE_B64} characters)"

echo ""
echo -e "${GREEN}📋 GitHub Secrets to Add:${NC}"
echo ""

echo "1. **RELEASE_KEYSTORE_B64**"
echo "   Description: Base64 encoded release keystore for signing APKs"
echo "   Value: (Copy the base64 string below)"
echo ""
echo "$KEYSTORE_B64"
echo ""

echo "2. **KEYSTORE_PASSWORD**"
echo "   Description: Password for the release keystore"
echo "   Value: lifeproblemsolver2024"
echo ""

echo "3. **KEY_ALIAS**"
echo "   Description: Alias for the release key"
echo "   Value: release-key"
echo ""

echo "4. **KEY_PASSWORD**"
echo "   Description: Password for the release key"
echo "   Value: lifeproblemsolver2024"
echo ""

echo "5. **OPENAI_API_KEY**"
echo "   Description: OpenAI API key for the app"
echo "   Value: [Your actual OpenAI API key]"
echo ""

echo -e "${YELLOW}📝 Instructions:${NC}"
echo "1. Go to: https://github.com/gunainvestor/GenAI_life-problem-solver/settings/secrets/actions"
echo "2. Click 'New repository secret' for each secret above"
echo "3. Copy the values exactly as shown"
echo "4. Save each secret"
echo ""

echo -e "${GREEN}🔧 Next Steps:${NC}"
echo "1. Add all secrets to GitHub"
echo "2. Update build.gradle.kts to use environment variables"
echo "3. Test the CI/CD workflow"
echo ""

# Create a copy-paste friendly version
echo -e "${BLUE}📋 Copy-Paste Values:${NC}"
echo ""

echo "=== RELEASE_KEYSTORE_B64 ==="
echo "$KEYSTORE_B64"
echo ""

echo "=== KEYSTORE_PASSWORD ==="
echo "lifeproblemsolver2024"
echo ""

echo "=== KEY_ALIAS ==="
echo "release-key"
echo ""

echo "=== KEY_PASSWORD ==="
echo "lifeproblemsolver2024"
echo ""

echo "=== OPENAI_API_KEY ==="
echo "[Replace with your actual OpenAI API key]"
echo ""

print_status "Setup helper completed!"
print_info "Remember to add your actual OpenAI API key for the OPENAI_API_KEY secret" 