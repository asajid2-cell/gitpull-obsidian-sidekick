# Release Notes

## Artifact

- APK: `apk/gitpull-debug.apk`
- SHA-256: `373c197b5ee94995fbedc3ee01b1eace70c523c8e04a96cb8275b9d1e936063e`
- Android package: `dev.gitpull.app`
- Minimum Android version: API 26

This is a debug-signed APK produced by the local Android build. It is suitable for side-loading and testing, not Play Store distribution.

Browser GitHub sign-in is implemented with GitHub OAuth Device Flow and is enabled in this APK with the app's public OAuth client ID. The token fallback remains available.

This build keeps the GitHub device code visible in the app, adds a `Copy code` button, and re-checks sign-in when the app resumes after GitHub approval.

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
