# gitpull Design System

gitpull is a small Android utility app. It does not replace Obsidian. Its job is to refresh a normal Android folder from a GitHub vault snapshot, then hand the user back to Obsidian or an external PDF app.

## Design Intent

The interface should feel like a quiet developer tool:

- One obvious action: `Pull`.
- GitHub is always treated as the source of truth.
- Obsidian remains the place for reading and editing notes.
- PDFs are surfaced only as convenience shortcuts.
- Failure states preserve the last usable local snapshot.

## Visual References

| Board | File |
| --- | --- |
| Colors and typography | `images/01-colors-typography.png` |
| Layout and spacing | `images/02-layout-spacing.png` |
| Controls and states | `images/03-controls-states.png` |
| Cards and lists | `images/04-cards-lists.png` |
| User flow | `images/05-user-flow.png` |
| APK logo | `images/06-apk-logo.png` |

The implementation spec is in `spec/gitpull-ui-spec.json`.

## Color Tokens

| Token | Hex | Usage |
| --- | --- | --- |
| `textPrimary` | `#111827` | App title, important labels, primary button background |
| `textSecondary` | `#334155` | Secondary labels and summary text |
| `textMuted` | `#64748B` | Captions, paths, inactive nav |
| `background` | `#F8FAFC` | App background |
| `surface` | `#FFFFFF` | Cards, app bars, fields |
| `surfaceSubtle` | `#F1F5F9` | Disabled or neutral panels |
| `line` | `#E2E8F0` | Card borders and dividers |
| `accent` | `#7C3AED` | Active tab, focus, selected states |
| `success` | `#16A34A` | Completed pull state |
| `warning` | `#D97706` | Source-of-truth warning |
| `danger` | `#DC2626` | Failed pull or destructive actions |

Use accent color sparingly. The app should read mostly white, graphite, and clean gray.

## Typography

Use `Inter`, `Roboto`, or the Android system sans-serif.

| Role | Size | Line Height | Weight |
| --- | ---: | ---: | ---: |
| Display | 32 | 40 | 700 |
| Screen title | 22 | 28 | 700 |
| Section title | 16 | 24 | 650 |
| Body | 15 | 22 | 400 |
| Body strong | 15 | 22 | 600 |
| Caption | 12 | 16 | 500 |
| Button | 16 | 20 | 700 |

Keep text compact. This is a utility app, so avoid marketing-style hero typography except for the large Pull button label.

## Spacing

Base spacing scale:

- `4dp` micro alignment
- `8dp` compact internal spacing
- `12dp` row gaps
- `16dp` card padding
- `20dp` screen horizontal padding
- `24dp` section gaps
- `32dp` large vertical separation

Minimum touch target is `48dp`. The primary Pull button is intentionally large at about `112dp` high.

## Radius and Elevation

Cards and inputs use restrained corners:

- Inputs: `8dp`
- Cards: `8dp`
- Large primary action: `12dp`
- Chips: pill radius

Use borders before shadows. If elevation is needed, use a subtle `0 1px 2px` style shadow only.

## Navigation

The app has three primary tabs:

- `Pull`
- `PDFs`
- `Settings`

The setup screen appears first and blocks the main flow until the repo URL and destination folder are configured.

## Core Screens

### Setup

Purpose: collect source and destination.

Required controls:

- GitHub repo field
- Branch field, default `main`
- Destination folder selector
- Optional GitHub token field
- GitHub repositories browser with Load and Use actions
- Source-of-truth warning
- Save action

Validation:

- Accept `https://github.com/{owner}/{repo}`.
- Strip a trailing `.git` suffix.
- Require folder access before first pull.
- Store token only in encrypted Android storage.
- Use the token to load repositories from GitHub, then fill repo and branch when a repo is selected.

### Pull

Purpose: refresh the local vault snapshot.

Required controls:

- Repo summary card
- Large `Pull` button
- Progress/status panel
- Last pulled timestamp
- `Open Obsidian` secondary action

States:

- Idle: ready to refresh
- Pulling: download/extract/refresh progress
- Success: snapshot ready
- Error: previous snapshot kept
- Missing permission: request folder access again
- Offline: explain that internet is needed to pull

### PDFs

Purpose: collect all PDFs from the pulled vault.

Required controls:

- Search field
- PDF count
- PDF rows with filename and relative path
- Open-with action
- Empty state

Tap opens Android's chooser with a content URI. Samsung Notes can be selected if installed.

### Settings

Purpose: manage configuration and safety.

Required controls:

- Change repository
- Change branch
- Browse GitHub repositories
- Change destination folder
- Add/remove GitHub token
- Clear local snapshot
- Source-of-truth warning

Destructive actions require confirmation.

## Component Rules

### Primary Pull Button

The Pull button is the center of the app. It should be large, dark, and easy to hit.

States:

- `Pull`
- `Pulling...`
- `Retry Pull`
- Disabled when setup is incomplete

During pulling, disable repeated taps and show status text below or inside the status card.

### Cards

Use cards for contained information only:

- Repo summary
- Pull status
- PDF rows
- Warning panels
- Credential state

Do not place cards inside cards.

### Lists

PDF rows should be dense but readable:

- Filename on first line
- Relative path on second line
- File icon on the left
- Open-with affordance on the right

Long paths should truncate in the middle or tail, never overflow.

### Warnings

The source-of-truth warning should be visible during setup and available in settings:

`GitHub is source of truth. Pulling replaces this local snapshot.`

Use warning color and icon, but do not make the whole app feel alarming.

## Interaction Model

Pull flow:

1. Validate repo, branch, token, and folder permission.
2. Download the GitHub archive into app cache.
3. Extract into an app-private temp directory.
4. Strip the archive root folder.
5. Refresh the selected vault folder.
6. Remove local files not present in the snapshot.
7. Save last pull metadata.
8. Rebuild PDF index.

Failure rules:

- Download failure leaves the existing vault untouched.
- Extraction failure leaves the existing vault untouched.
- Permission failure asks the user to reselect the folder.
- Token failure sends the user to credentials/settings.
- Repository browsing without a token shows a direct missing-token message.

## APK Logo Direction

Use the generated logo board as a direction, not as a final vector source.

Logo requirements:

- Original mark, no exact GitHub or Obsidian logo.
- Reads at launcher size.
- Works in square, circle, and rounded-square masks.
- Has a monochrome version.
- Uses graphite, violet, white, and a small success green accent.

## Implementation Notes

Recommended implementation stack:

- Kotlin
- Jetpack Compose
- Material 3
- Android Storage Access Framework
- OkHttp
- Zip archive download/extraction
- Jetpack Security for token storage

The UI should be buildable directly from `spec/gitpull-ui-spec.json`.
