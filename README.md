# gitpull

`gitpull` is a small Android utility that manually refreshes a local Obsidian vault folder from a GitHub repository snapshot. It is pull-only: it never commits or pushes.

## Build

```powershell
.\gradlew.bat clean assembleDebug
```

The debug APK is produced at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Latest audited APK SHA-256:

```text
8e07c5cd784adf8460dba48fe5abcd3b62478c41a4cee197a8818ac723a9f172
```

## Test

```powershell
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```

## Runtime Flow

1. Paste a GitHub token when you want private/account repo browsing.
2. Tap `Load` in the GitHub repositories card and choose the vault repo, or paste a GitHub repo URL manually.
3. Keep `main` or enter another branch.
4. Choose an Android destination folder using the folder picker.
5. Tap `Pull`.
6. Open the same folder in Obsidian Mobile.
7. Use the `PDFs` tab to open pulled PDFs through Android's open-with chooser.

The design system is in `design/`. Planning and validation notes are in `docs/`.

Pixel 9 Pro XL walkthrough screenshots are in `docs/assets/screenshots/`, with the screen-by-screen walkthrough at `docs/tutorials/visual-walkthrough.md`.

## Current Validation State

The app builds, installs, passes unit/instrumentation/lint validation, browses GitHub repositories with a token, pulls public GitHub snapshots, refreshes SAF-selected folders, indexes PDFs, opens pulled folders in Obsidian Mobile, and has live private GitHub fixture validation for Markdown, `.obsidian`, image, and PDF assets.

Live private GitHub validation used a controlled fixture repository containing Markdown, `.obsidian`, image, and PDF assets. The exact private fixture identity is not required for normal use.

PDF files open through Android's standard app chooser, so any installed PDF reader or annotation app can handle them.

The current validation summary is in `docs/validation/VALIDATION_SUMMARY.md`.

To run the validation suite:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```
