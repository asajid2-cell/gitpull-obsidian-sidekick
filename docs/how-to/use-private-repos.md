# Use Private GitHub Repositories

`gitpull` supports private repositories with a GitHub token.

## Create A Token

Create a fine-grained GitHub token with read-only access to the repositories you want to pull.

Minimum practical scope:

- Repository contents: read-only.

## Use The Token

1. Open `gitpull`.
2. Paste the token in the GitHub token field.
3. Tap `Save`.
4. Tap `Load` in the GitHub repositories card.
5. Choose the repository.
6. Pull as usual.

The token is stored through Android encrypted preferences in the app's private storage.
