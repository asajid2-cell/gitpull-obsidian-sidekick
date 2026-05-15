# Use Private GitHub Repositories

`gitpull` supports private repositories with browser GitHub sign-in or the token fallback.

## Use Browser Sign-In

Browser sign-in is enabled in the included APK with the app's public GitHub OAuth client ID.

When browser sign-in is enabled:

1. Open `gitpull`.
2. Tap `Sign in with GitHub`.
3. Tap `Copy code`.
4. Paste the code into the GitHub browser window.
5. Return to `gitpull`. The same sign-in session stays visible; the app re-checks when it resumes, and you can tap `Check sign-in` to retry immediately. After approval completes, the app should say it signed in and then load repositories.
6. Tap `Load` in the GitHub repositories card.
7. Tap `Load more` if your repository is not on the first page.
8. Choose the repository.
9. Pull as usual.

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
