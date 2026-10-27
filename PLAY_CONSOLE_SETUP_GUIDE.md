# Google Play Console Setup Guide for "Swipe Clean"

Use this guide to complete every step required in your Google Play Console dashboard under **"Finish setting up your app"** for Open Testing, Closed Testing, and Production release.

---

## 1. Set Privacy Policy
Google Play requires a public URL for your Privacy Policy.

### Hosted Privacy Policy Copy (Ready to publish on GitHub Gist / Notion / Web):
```markdown
# Privacy Policy for Swipe Clean

Effective Date: October 2026
App Version: 3.0

Swipe Clean ("we", "our", or "the app") respects your privacy. This Privacy Policy explains our data practices.

### 1. Zero Cloud Data Collection
Swipe Clean operates 100% locally on your Android device. We do not collect, store, transmit, or share any personal data, photos, videos, filenames, file locations, metadata, device identifiers, or analytics with external servers or third parties.

### 2. Device Media Permissions
The app requests access to your device media library solely to:
- Render local photos and videos within your sorting sessions.
- Allow you to organize, review, and initiate local deletions via Android system APIs.
No media content is ever uploaded off your device.

### 3. Local Data Storage
All session states, decision histories (Keep, Trash, Skip), and user preferences are stored securely in a local, sandboxed SQLite (Room) database on your Android device.

### 4. No Advertisements or Third-Party SDKs
This app contains no advertising networks, no tracking SDKs, no behavioral monitoring, and no telemetry analytics.

### 5. Contact
For questions regarding this policy, contact: bholak993@gmail.com
```

> **How to get a URL:** Copy the markdown above into a free [GitHub Gist](https://gist.github.com/) or Notion page and paste the public link into the **Set privacy policy** field in Play Console.

---

## 2. Sign-in Details
- **Selection:** "App functionality is available without special access permissions" (No credentials required).
- **Explanation:** The app operates completely locally on the user's device and does not require account creation or login.

---

## 3. Ads
- **Selection:** "No, my app does not contain ads".

---

## 4. Content Rating
- Complete the Content Rating questionnaire:
  - **Category:** Utilities, Productivity, or Tools
  - **Violence / Sex / Hate / Gambling / Drugs:** No to all questions.
  - **Expected Rating:** **EVERYONE** (PEGI 3, USK 0, ESRB Everyone).

---

## 5. Target Audience
- **Target Age Groups:** Select **13-15**, **16-17**, and **18 and over** (13+).
- **Could your store listing intentionally appeal to children?** Select **No**.

---

## 6. Data Safety Questionnaire
Answer as follows in the Data Safety form:

1. **Does your app collect or share any of the required user data types?**
   - Answer: **No**
2. **Is all user data collected by your app encrypted in transit?**
   - Answer: **N/A (No data is collected or transmitted)**
3. **Account creation / User deletion:**
   - Answer: **My app does not allow users to create an account.**

---

## 7. Government Apps
- **Selection:** "No" (This app is not affiliated with a government agency).

---

## 8. Financial Features
- **Selection:** "My app doesn't provide any financial features".

---

## 9. Health & Medical Features
- **Selection:** "My app isn't a health or medical app".

---

## 10. App Category and Contact Details
- **App Type:** App
- **Category:** Tools (or Photography)
- **Contact Email:** `bholak993@gmail.com`
- **Phone / Website:** Optional (Can leave blank)

---

## 11. Store Listing Metadata

### App Name (Title - Max 30 chars):
`Swipe Clean - Photo Cleaner`

### Short Description (Max 80 chars):
`Swipe to clean photos and videos, free up storage space, and keep gallery neat.`

### Full Description:
```
Swipe Clean makes organizing your photo and video gallery effortless, fast, and 100% private.

Declutter your device without stress! Swipe right to keep your best memories, and swipe left to queue unwanted items for removal.

KEY FEATURES:

✨ Intuitive Swipe Sorting
Quickly review your camera roll, screenshots, or large videos one card at a time with smooth swipe gestures and visible action buttons.

🔒 100% On-Device & Private
Your photos and videos never leave your phone. Swipe Clean processes everything locally in sandboxed local storage without external servers.

📹 Smooth Video Player
Preview active video cards directly in full quality with autoplay, mute/unmute toggles, elapsed time progress, and full playback controls.

⚡ Instant Multi-Step Undo
Made a mistake? Tap Undo anytime during a session to immediately revert your previous Keep or Trash decision.

📊 Storage Impact Statistics
Track your exact progress with real-time statistics on items reviewed, items kept, trash queue, and total freed storage space.

📁 Multiple Source Support
Sort through your local device library or select individual albums with zero broad permissions using the Android Photo Picker.

Take control of your gallery today with Swipe Clean!
```

---

## Next Steps for Testing & Production:
1. Complete the 11 items above in your Google Play Console Dashboard.
2. In Play Console, navigate to **Testing -> Open testing** (or **Closed testing**).
3. Create a new track, upload your latest app bundle (`.aab` / compiled build), and click **Save & Review release**.
4. Once reviewed by Google Play, your open/closed testing track will be active!
