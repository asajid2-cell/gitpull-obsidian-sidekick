# App Behavior Reference

## Pull Model

`gitpull` downloads GitHub repository archives and extracts them locally. It does not use `git clone`, and it does not write to GitHub.

## Snapshot Replacement

Pulls are staged before replacing the destination. If download or extraction fails, the previous snapshot remains usable.

## Repository Input

The app supports:

- Manual GitHub repository URL entry.
- Authenticated repository browsing with a GitHub token.

## Destination Folder

The destination is selected through Android's Storage Access Framework. This lets the user choose a normal folder that Obsidian Mobile can also open.

## PDF Handling

PDF files are indexed after pull completion and opened through Android `ACTION_VIEW` with `application/pdf`.
