# 🔧 How to Enable GitHub Pages - Step by Step

## The Problem
Your landing page files are committed and pushed, but GitHub Pages needs to be **enabled** in your repository settings.

## Solution: Enable GitHub Pages

### Step 1: Go to Repository Settings
1. Open your browser and go to:
   **https://github.com/gunainvestor/GenAI_life-problem-solver**

2. Click on the **"Settings"** tab (at the top of the repository, next to "Code", "Issues", etc.)

### Step 2: Navigate to Pages Section
1. In the left sidebar, scroll down and click on **"Pages"**

### Step 3: Configure GitHub Pages
You have **TWO options**:

#### Option A: Use GitHub Actions (Recommended - Already Set Up!)
1. Under **"Source"**, select:
   - **"Deploy from a branch"** → Change this to **"GitHub Actions"**
2. Click **"Save"**
3. The workflow will automatically deploy your site!

#### Option B: Deploy from Branch (Alternative)
1. Under **"Source"**, select:
   - **Branch**: `main`
   - **Folder**: `/ (root)`
2. Click **"Save"**

### Step 4: Wait for Deployment
- After saving, GitHub will start deploying your site
- You can check the deployment status in the **"Actions"** tab
- It usually takes 1-2 minutes

### Step 5: Access Your Site
Once deployed, your site will be available at:
**https://gunainvestor.github.io/GenAI_life-problem-solver**

## Troubleshooting

### If you see "404" or "Page not found":
1. **Wait 2-3 minutes** - GitHub Pages can take a moment to deploy
2. **Check the Actions tab** - Look for any deployment errors
3. **Clear your browser cache** - Try incognito/private mode
4. **Verify repository is public** - Private repos need GitHub Pro for Pages

### If the Actions workflow fails:
1. Go to the **"Actions"** tab in your repository
2. Click on the failed workflow run
3. Check the error messages
4. Make sure the workflow file (`.github/workflows/deploy-pages.yml`) is correct

### Manual Trigger (if needed):
1. Go to **"Actions"** tab
2. Select **"Deploy static site to GitHub Pages"** workflow
3. Click **"Run workflow"** → **"Run workflow"** button

## Quick Checklist
- ✅ `index.html` exists in root directory
- ✅ `.nojekyll` file exists
- ✅ Files are committed and pushed to `main` branch
- ⏳ **GitHub Pages enabled in Settings** ← You need to do this!
- ⏳ **Source set to "GitHub Actions" or "Deploy from branch"** ← You need to do this!

---

**After enabling, your site should be live within 1-2 minutes!** 🚀

