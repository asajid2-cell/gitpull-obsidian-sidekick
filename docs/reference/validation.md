# Validation Reference

Latest release validation command:

```powershell
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```

Result:

- Build: PASS
- Unit tests: PASS, 25 tests, 0 failures, 0 errors, 1 skipped live fixture test
- Connected Android tests: PASS, 8 tests, 0 failures on `Pixel_9_Pro_XL(AVD) - 16`
- Lint: PASS, 0 errors
- GitHub OAuth Device Flow client tests: PASS, device-code request, token polling, and pending-authorization handling against a mock server
- Live GitHub OAuth Device Flow smoke: PASS, `POST https://github.com/login/device/code` returned a user code and `https://github.com/login/device`
- Visual walkthrough: PASS, Pixel 9 Pro XL screenshots captured at 1344x2992 with title and bottom navigation outside system-bar cutouts.

Private GitHub validation used a controlled fixture repository containing Markdown, `.obsidian`, image, and PDF assets. The exact private fixture identity is intentionally not required for normal use.
