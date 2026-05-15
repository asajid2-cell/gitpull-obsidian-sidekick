# Validation Summary

Latest public release validation passed on 2026-05-15.

## Command

```powershell
$env:ANDROID_HOME='<path-to-android-sdk>'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:ANDROID_SERIAL='emulator-5554'
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```

## Result

- Build: PASS
- Unit tests: PASS, 26 tests, 0 failures, 0 errors, 1 skipped live fixture test
- Connected Android tests: PASS, 9 tests on `Pixel_9_Pro_XL(AVD) - 16`
- Lint: PASS, 0 errors
- APK package: `dev.gitpull.app`
- APK SHA-256: `c59e647158a1f87bc18eab8ebdd9098f1a430e37c6b118576b2ba976e1816a8f`
- Live GitHub OAuth Device Flow smoke: PASS, `POST https://github.com/login/device/code` returned a user code and `https://github.com/login/device`; `POST https://github.com/login/oauth/access_token` returned `authorization_pending` for the unapproved live code.
- Visual walkthrough: PASS, Pixel 9 Pro XL screenshots captured at 1344x2992 after the Android safe-area fix.

## Coverage

Validation covered:

- GitHub repository parsing and authenticated request headers.
- GitHub OAuth Device Flow request, polling, pending-authorization handling, persisted in-progress sign-in restoration, and manual check controls.
- GitHub repository browsing uses paginated API loads and does not fetch every repo at once.
- ZIP archive extraction, root stripping, and zip-slip rejection.
- Snapshot refresh and previous-snapshot preservation after failure.
- PDF indexing and Android open-with handoff.
- Secure token storage on Android.
- Setup, Pull, PDFs, Settings, Storage Access Framework folder selection, GitHub sign-in enabled state for the configured build, sign-in resume polling, persisted pending sign-in after activity recreation, and touch targets.
- Branch Drop launcher icon vector selected and packaged.
- Live private GitHub fixture validation with a controlled vault fixture.
- Obsidian Mobile folder-open behavior.

PDF files open through Android's standard app chooser, so any installed PDF reader or annotation app can handle them.

## Repository Cleanup

Generated emulator dumps, third-party APKs, local machine paths, and scratch files are not included in this branch.
