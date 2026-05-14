# Security

## Supported Version

Only the latest public release is supported for security review.

## Reporting

Open a private GitHub security advisory if available, or contact the repository owner through GitHub.

Do not publish GitHub tokens, vault contents, or device screenshots in public issues.

## Token Guidance

Browser sign-in is preferred when the APK is built with a GitHub OAuth client ID. If you use the token fallback, use a fine-grained GitHub token with read-only repository contents access. Revoke saved GitHub credentials if a device is lost or a token is pasted into the wrong app.
