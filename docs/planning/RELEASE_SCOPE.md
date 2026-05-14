# Release Scope

`gitpull` is an Android sidekick app for manually pulling a GitHub repository snapshot into a local Android folder that Obsidian Mobile can open as a vault.

## In Scope

- Manual repository URL entry.
- Browser GitHub sign-in through GitHub OAuth Device Flow.
- GitHub token fallback for builds or users that do not use browser sign-in.
- Authenticated repository browsing.
- Pull-only archive download from GitHub.
- Snapshot extraction and folder refresh.
- Failure preservation for the previous usable snapshot.
- PDF indexing and Android open-with handoff.
- Obsidian Mobile folder compatibility.

## Out Of Scope

- Pushing, committing, merging, or resolving Git conflicts.
- Background sync.
- Obsidian Sync replacement with bidirectional merge behavior.
- Direct integration with one specific PDF reader. PDFs open through Android's standard app chooser.
- Play Store release packaging.

## Release Branches

- `main`: minimal public release package with APK, checksums, release notes, and user docs.
- `static`: source/design/planning branch.
