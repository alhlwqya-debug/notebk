# NoteBk = Pepers UI and behaviour

`MainActivity` shows the splash screen and then opens `WorkLedgerScreen`, which is the Pepers
interface unchanged (shops, months, pieces, numeric and individual registration, expenses,
statistics, PDF, backup, sync, reminders). The earlier NoteBk screens (home, bottom bar, tailor
ledger, shops, statistics, report, settings) and their `AppDatabase` data layer were removed from
the build; they remain in the earlier archive.

## Mapping
- database/schema -> `data/local/LedgerDatabase.kt` (file `add_paper.db`, kept so existing data still opens)
- records -> `domain/model/WorkModels.kt`
- numeric ledger -> `ui/screen/ledger/work/NumericLedger.kt`, `WorkLedgerScreen.kt`, `LedgerDialogs.kt`
- individual ledger -> `ui/screen/individual/work/CustomerLedger.kt`
- drawer -> `ui/navigation/WorkDrawerContent.kt`; settings dialog -> `ui/screen/settings/WorkSettingsDialog.kt`
- PDF -> `data/report/`; auth and sync -> `data/cloud/`; backup -> `security/`; reminders -> `notification/`

Screen bodies are byte-identical to the original Pepers files apart from package/import lines and
the renamed types (`Database` -> `LedgerDatabase`, `WorkLogSheet` -> `WorkLedgerScreen`).
Backup archives keep the original `database/add_paper.db` entry name, so old backups restore.
