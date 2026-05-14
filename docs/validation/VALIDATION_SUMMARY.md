# Validation Summary

Latest public release validation passed on 2026-05-14.

## Command

```powershell
$env:ANDROID_HOME='<path-to-android-sdk>'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:ANDROID_SERIAL='emulator-5556'
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```

## Result

- Build: PASS
- Unit tests: PASS
- Connected Android tests: PASS, 7 tests on `Pixel_9_Pro_XL(AVD) - 16`
- Lint: PASS, 0 errors
- APK package: `dev.gitpull.app`
- Visual walkthrough: PASS, Pixel 9 Pro XL screenshots captured at 1344x2992 after the Android safe-area fix.

## Coverage

Validation covered:

- GitHub repository parsing and token-backed request headers.
- ZIP archive extraction, root stripping, and zip-slip rejection.
- Snapshot refresh and previous-snapshot preservation after failure.
- PDF indexing and Android open-with handoff.
- Secure token storage on Android.
- Setup, Pull, PDFs, Settings, Storage Access Framework folder selection, and touch targets.
- Live private GitHub fixture validation with a controlled vault fixture.
- Obsidian Mobile folder-open behavior.

PDF files open through Android's standard app chooser, so any installed PDF reader or annotation app can handle them.

## Repository Cleanup

Generated emulator dumps, third-party APKs, local machine paths, and scratch files are not included in this branch.
