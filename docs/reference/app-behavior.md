# App Behavior Reference

## Pull Model

`gitpull` downloads GitHub repository archives and extracts them locally. It does not use `git clone`, and it does not write to GitHub.

## Snapshot Replacement

Pulls are staged before replacing the destination. If download or extraction fails, the previous snapshot remains usable.

## Repository Input

The app supports:

- Manual GitHub repository URL entry.
- Browser GitHub sign-in with the app's public GitHub OAuth client ID.
- Authenticated repository browsing with the saved GitHub credential.
- A token fallback when browser sign-in is not enabled.

## Destination Folder

The destination is selected through Android's Storage Access Framework. This lets the user choose a normal folder that Obsidian Mobile can also open.

## PDF Handling

PDF files are indexed after pull completion and opened through Android `ACTION_VIEW` with `application/pdf`.
