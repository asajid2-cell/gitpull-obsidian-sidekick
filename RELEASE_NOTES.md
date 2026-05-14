# Release Notes

## Artifact

- APK: `apk/gitpull-debug.apk`
- SHA-256: `ca1d304aa02907c2aeeab091e676c6fc29e89959e9b7f3406f306313e2d67849`
- Android package: `dev.gitpull.app`
- Minimum Android version: API 26

This is a debug-signed APK produced by the local Android build. It is suitable for side-loading and testing, not Play Store distribution.

Browser GitHub sign-in is implemented with GitHub OAuth Device Flow. The included APK was built without a project-specific OAuth client ID, so use the token fallback for this binary or rebuild from the `static` branch with `GITPULL_GITHUB_CLIENT_ID` to enable the browser sign-in button.

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
