# gitpull Obsidian Sidekick

`gitpull` is a small Android app for manually refreshing an Obsidian vault folder from GitHub. It is pull-only: it downloads repository snapshots and writes them into the Android folder you choose, but it never commits or pushes.

## Download

The APK is included in this release branch:

```text
apk/gitpull-debug.apk
```

This is a debug-signed APK for sideload testing. It is not a Play Store release build.

Verify the APK before installing:

```text
8e07c5cd784adf8460dba48fe5abcd3b62478c41a4cee197a8818ac723a9f172  apk/gitpull-debug.apk
```

## What It Does

- Browse GitHub repositories with a saved or pasted token.
- Pull a selected repository branch into an Android folder.
- Preserve the previous usable snapshot if a pull fails.
- Index pulled PDFs in a dedicated tab.
- Open PDFs through Android's open-with flow.
- Let Obsidian Mobile open the refreshed folder as a vault.

## Screenshots

Pixel 9 Pro XL walkthrough screenshots are included in `docs/assets/screenshots/`.

<p>
  <img src="docs/assets/screenshots/03-pull-ready.png" alt="gitpull Pull tab on Pixel 9 Pro XL" width="320">
</p>

## Documentation

The docs follow the Diataxis structure:

- Tutorial: `docs/tutorials/getting-started.md`
- Visual walkthrough: `docs/tutorials/visual-walkthrough.md`
- How-to guides: `docs/how-to/`
- Reference: `docs/reference/`
- Explanation: `docs/explanation/`

## Validation

Latest release validation:

```text
assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest
PASS
Unit tests: 22 tests, 0 failures, 1 gated live test skipped without token
Connected Android tests: 7 tests, 0 failures
Lint: 0 errors
Visual walkthrough: Pixel 9 Pro XL emulator at 1344x2992
```

PDF files open through Android's standard app chooser, so any installed PDF reader or annotation app can handle them.

## License

This repository is public for review and APK distribution. No reuse license is granted unless the repository owner replaces `LICENSE` with an open-source license later.
