# Contributing

This source branch is published for review. External contributions are not actively accepted yet.

Before sharing changes:

1. Build the APK.
2. Run unit tests.
3. Run connected Android tests on an emulator or device.
4. Do not commit tokens, local paths, generated build output, emulator dumps, third-party APKs, or private vault content.

Expected validation command:

```powershell
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```
