# Release Notes

## Artifact

- APK: `apk/gitpull-debug.apk`
- SHA-256: `13d691c49ca48d99c90663a0976df0b80329aeb045fc90b7b346a70535602baf`
- Android package: `dev.gitpull.app`
- Minimum Android version: API 26

This is a debug-signed APK produced by the local Android build. It is suitable for side-loading and testing, not Play Store distribution.

Browser GitHub sign-in is implemented with GitHub OAuth Device Flow and is enabled in this APK with the app's public OAuth client ID. The token fallback remains available.

This build keeps the GitHub device code visible in the app, adds a `Copy code` button, stores the in-progress approval session synchronously before leaving for GitHub, re-checks that same session when you return, keeps the pending code active after transient poll failures, and keeps the signed-in message visible while repositories load.

The Android connected test suite includes approved GitHub Device Flow, pending-session restore, committed pending-session restore, and transient poll-failure coverage through the real app UI.

Repository browsing now loads one GitHub page at a time and exposes `Load more` for the next page, so accounts with many repositories do not fetch everything at once.

The APK uses the selected Branch Drop launcher logo.

## Final Checks

The release was checked with:

```powershell
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```

Result: PASS.

The current APK also includes the Android safe-area fix for larger phones and tablets. Visual walkthrough screenshots were retaken on a Pixel 9 Pro XL emulator at 1344x2992.

## Notes

PDF files open through Android's standard app chooser. Install the reader or annotation app you prefer, then choose it when opening a PDF from `gitpull`.
