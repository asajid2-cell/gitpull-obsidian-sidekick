# Security And Data Handling

`gitpull` is pull-only. It has no code path for committing or pushing to GitHub.

GitHub tokens are used for:

- Listing repositories through GitHub's authenticated API.
- Downloading private repository archives.

Tokens are stored in Android encrypted preferences inside the app's private storage. A read-only fine-grained GitHub token is recommended.

The app writes only to the Android folder selected by the user through the Storage Access Framework, or to its private app folder when no external folder is selected.
