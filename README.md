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
396e6de68ef0a5043cacd8637b0f8365a2e50e1e8f370e0901f2c79018e39e65  apk/gitpull-debug.apk
```

The included APK has browser GitHub sign-in enabled through the app's public OAuth client ID. In-progress GitHub approval sessions are kept when you return from the browser, and the app keeps sign-in success visible even if repository loading has a separate failure. The token fallback is still available.

## What It Does

- Browse GitHub repositories after browser sign-in, or with the token fallback, one page at a time.
- Copy the GitHub device code, keep the same login session active after returning from GitHub, complete sign-in without starting over, and then load repositories.
- Use the Branch Drop launcher logo.
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
Unit tests: 26 tests, 0 failures, 1 gated live test skipped without token
Connected Android tests: 10 tests, 0 failures
Lint: 0 errors
Visual walkthrough: Pixel 9 Pro XL emulator at 1344x2992
```

PDF files open through Android's standard app chooser, so any installed PDF reader or annotation app can handle them.

## License

This repository is public for review and APK distribution. No reuse license is granted unless the repository owner replaces `LICENSE` with an open-source license later.
