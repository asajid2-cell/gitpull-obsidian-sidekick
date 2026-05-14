# Validation Reference

Latest release validation command:

```powershell
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```

Result:

- Build: PASS
- Unit tests: PASS, 22 tests, 0 failures
- Connected Android tests: PASS, 7 tests, 0 failures on `Pixel_9_Pro_XL(AVD) - 16`
- Lint: PASS, 0 errors
- Visual walkthrough: PASS, Pixel 9 Pro XL screenshots captured at 1344x2992 with title and bottom navigation outside system-bar cutouts.

Private GitHub validation used a controlled fixture repository containing Markdown, `.obsidian`, image, and PDF assets. The exact private fixture identity is intentionally not required for normal use.
