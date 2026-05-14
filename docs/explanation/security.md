# Security And Data Handling

`gitpull` is pull-only. It has no code path for committing or pushing to GitHub.

GitHub credentials are used for:

- Listing repositories through GitHub's authenticated API.
- Downloading private repository archives.

Browser sign-in uses GitHub OAuth Device Flow, so the Android app does not need a client secret. The saved access token is stored in Android encrypted preferences inside the app's private storage.

The token fallback is still available. A read-only fine-grained GitHub token is recommended when using that fallback.

The app writes only to the Android folder selected by the user through the Storage Access Framework, or to its private app folder when no external folder is selected.
