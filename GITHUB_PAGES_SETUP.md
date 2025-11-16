# GitHub Pages Setup Guide

## Quick Setup Instructions

Your landing page is ready! Follow these steps to publish it to GitHub Pages:

### Step 1: Enable GitHub Pages

1. Go to your repository: `https://github.com/gunainvestor/GenAI_life-problem-solver`
2. Click on **Settings** (in the repository menu)
3. Scroll down to **Pages** (in the left sidebar)
4. Under **Source**, select:
   - **Branch**: `main` (or `master`)
   - **Folder**: `/ (root)`
5. Click **Save**

### Step 2: Wait for Deployment

- GitHub Pages will automatically deploy your site
- It usually takes 1-2 minutes
- You'll see a green checkmark when it's ready

### Step 3: Access Your Landing Page

Your landing page will be available at:
**https://gunainvestor.github.io/GenAI_life-problem-solver**

### What's Included

✅ Beautiful, modern landing page design
✅ Responsive layout (works on mobile, tablet, desktop)
✅ QR code generator (scans to the landing page URL)
✅ Feature showcase
✅ Pricing plans
✅ Statistics section
✅ Direct APK download link
✅ Smooth animations and transitions

### Files Created

- `index.html` - Main landing page
- `.nojekyll` - Ensures GitHub Pages serves files correctly

### Customization

You can customize the landing page by editing `index.html`:
- Colors: Edit CSS variables in the `:root` section
- Content: Modify the HTML sections
- Features: Add/remove feature cards
- Pricing: Update pricing plans

### Troubleshooting

**Page not loading?**
- Check that GitHub Pages is enabled in Settings
- Wait a few minutes for deployment
- Clear your browser cache

**QR code not showing?**
- Ensure you have internet connection (uses CDN)
- Check browser console for errors

**APK download not working?**
- Verify the APK file exists at `releases/LifeProblemSolver-v1.6.1-release.apk`
- Update the path in `index.html` if needed

### Next Steps

1. Commit and push the files to GitHub:
   ```bash
   git add index.html .nojekyll
   git commit -m "Add landing page with QR code"
   git push origin main
   ```

2. Enable GitHub Pages (see Step 1 above)

3. Share your landing page URL with others!

---

**Need help?** Check the [GitHub Pages documentation](https://docs.github.com/en/pages)

