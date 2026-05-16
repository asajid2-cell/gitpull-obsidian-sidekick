# gitpull

`gitpull` is a small Android utility that manually refreshes a local Obsidian vault folder from a GitHub repository snapshot. It is pull-only: it never commits or pushes.

## Build

```powershell
.\gradlew.bat clean assembleDebug
```

Browser GitHub sign-in is enabled by default with the public GitHub OAuth client ID in `gradle.properties`. To override it for a fork, build with another GitHub OAuth app client ID that has Device Flow enabled:

```powershell
.\gradlew.bat clean assembleDebug -PGITPULL_GITHUB_CLIENT_ID='<github-oauth-client-id>'
```

The client ID can also be supplied through the `GITPULL_GITHUB_CLIENT_ID` environment variable. Builds without a client ID keep the manual token fallback available and show the GitHub sign-in button as unavailable.

The debug APK is produced at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Latest audited APK SHA-256:

```text
13d691c49ca48d99c90663a0976df0b80329aeb045fc90b7b346a70535602baf
```

## Test

```powershell
$env:GRADLE_OPTS='-Xmx1024m -Dfile.encoding=UTF-8'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```

## Runtime Flow

1. Sign in with GitHub in the app, or paste a GitHub token only as a fallback.
2. Tap `Load` in the GitHub repositories card and choose the vault repo, or paste a GitHub repo URL manually.
3. Keep `main` or enter another branch.
4. Choose an Android destination folder using the folder picker.
5. Tap `Pull`.
6. Open the same folder in Obsidian Mobile.
7. Use the `PDFs` tab to open pulled PDFs through Android's open-with chooser.

The design system is in `design/`. Planning and validation notes are in `docs/`.

Pixel 9 Pro XL walkthrough screenshots are in `docs/assets/screenshots/`, with the screen-by-screen walkthrough at `docs/tutorials/visual-walkthrough.md`.

## Current Validation State

The app builds, installs, passes unit/instrumentation/lint validation, supports browser GitHub sign-in through GitHub's device flow, keeps in-progress GitHub approval sessions after returning from the browser, validates the approved sign-in path in an Android UI test, keeps sign-in success visible even if repository loading has a separate failure, keeps the same sign-in code active after transient poll failures, saves auth state synchronously before leaving for the browser, keeps a token fallback, loads GitHub repositories one page at a time, pulls public GitHub snapshots, refreshes SAF-selected folders, indexes PDFs, opens pulled folders in Obsidian Mobile, and has live private GitHub fixture validation for Markdown, `.obsidian`, image, and PDF assets.

Live private GitHub validation used a controlled fixture repository containing Markdown, `.obsidian`, image, and PDF assets. The exact private fixture identity is not required for normal use.

PDF files open through Android's standard app chooser, so any installed PDF reader or annotation app can handle them.

The current validation summary is in `docs/validation/VALIDATION_SUMMARY.md`.

To run the validation suite:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest --no-daemon --max-workers=1 --stacktrace
```
