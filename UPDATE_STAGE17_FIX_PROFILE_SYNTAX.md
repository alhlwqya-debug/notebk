# Stage 17 Fix — ProfilePage / Brand component compile repair

- Restored the ProfilePage invocation in WorkLedgerScreen.kt; the previous edit had accidentally pasted a second ProfilePage function declaration into the middle of the composable call.
- Reused the existing ProfilePage from ui/screen/settings/NotebkSettingsPages.kt.
- Restored image-picker, save validation, account-auth, and back callbacks.
- Updated NotebkWordmark to accept an optional Modifier so drawer usage with Modifier.fillMaxWidth() compiles.
- Kept the official notebk/AHD brand assets and existing database/report logic intact.
- targetSdk 36 edge-to-edge remains a warning and is not treated as a compile error.
