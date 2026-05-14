# Architecture

`gitpull` is intentionally small. The user chooses a GitHub repository and a local Android folder, then taps Pull when they want a fresh snapshot.

The app separates the workflow into a few pieces:

- Settings store repository, branch, folder, and token.
- GitHub clients download archives and list repositories.
- Pull service stages and refreshes the destination.
- PDF indexer scans the refreshed folder.
- Compose UI exposes Setup, Pull, PDFs, and Settings screens.

The app uses repository archives instead of a full Git client because the target behavior is one-way snapshot refresh. That keeps the Android app simpler and avoids merge/conflict behavior that belongs in Git tooling, not a tap-to-refresh sidekick.
