# Contributing

This repository is published for review and APK distribution. External contributions are not actively accepted yet.

For local development, use the `static` branch, which contains the Android source and validation notes.

Before proposing changes:

1. Build the APK.
2. Run unit tests.
3. Run connected Android tests on an emulator or device.
4. Do not commit tokens, local paths, generated build output, emulator dumps, or private vault content.

Expected validation command on the source branch:

```powershell
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```
