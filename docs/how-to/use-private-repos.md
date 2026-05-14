# Use Private GitHub Repositories

`gitpull` supports private repositories with browser GitHub sign-in or the token fallback.

## Use Browser Sign-In

Browser sign-in is enabled in the included APK with the app's public GitHub OAuth client ID.

When browser sign-in is enabled:

1. Open `gitpull`.
2. Tap `Sign in with GitHub`.
3. Enter the code shown in the app in the browser window.
4. Return to `gitpull`.
5. Tap `Load` in the GitHub repositories card.
6. Choose the repository.
7. Pull as usual.

## Use The Token Fallback

Create a fine-grained GitHub token with read-only access to the repositories you want to pull.

Minimum practical scope:

- Repository contents: read-only.

1. Open `gitpull`.
2. Paste the token in the token fallback field.
3. Tap `Save`.
4. Tap `Load` in the GitHub repositories card.
5. Choose the repository.
6. Pull as usual.

GitHub credentials are stored through Android encrypted preferences in the app's private storage.
