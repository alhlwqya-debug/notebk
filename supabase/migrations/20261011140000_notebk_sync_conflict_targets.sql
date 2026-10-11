-- NoteBK cloud sync compatibility migration.
-- Additive only: preserve existing rows and table identities.
-- Applied to Supabase project qieukleyxkvwygzxfgsm.

CREATE UNIQUE INDEX IF NOT EXISTS days_user_id_month_legacy_id_date_value_key
  ON public.days (user_id, month_legacy_id, date_value);

CREATE UNIQUE INDEX IF NOT EXISTS entries_user_id_day_legacy_id_piece_legacy_id_key
  ON public.entries (user_id, day_legacy_id, piece_legacy_id);

CREATE UNIQUE INDEX IF NOT EXISTS individual_entry_items_user_id_entry_legacy_id_piece_legacy_key
  ON public.individual_entry_items (user_id, entry_legacy_id, piece_legacy_id);

CREATE UNIQUE INDEX IF NOT EXISTS shops_user_record_key_uidx
  ON public.shops (user_id, record_key);
CREATE UNIQUE INDEX IF NOT EXISTS workers_user_record_key_uidx
  ON public.workers (user_id, record_key);
CREATE UNIQUE INDEX IF NOT EXISTS months_user_record_key_uidx
  ON public.months (user_id, record_key);
CREATE UNIQUE INDEX IF NOT EXISTS pieces_user_record_key_uidx
  ON public.pieces (user_id, record_key);
CREATE UNIQUE INDEX IF NOT EXISTS days_user_record_key_uidx
  ON public.days (user_id, record_key);
CREATE UNIQUE INDEX IF NOT EXISTS entries_user_record_key_uidx
  ON public.entries (user_id, record_key);
CREATE UNIQUE INDEX IF NOT EXISTS individual_entries_user_record_key_uidx
  ON public.individual_entries (user_id, record_key);
CREATE UNIQUE INDEX IF NOT EXISTS individual_entry_items_user_record_key_uidx
  ON public.individual_entry_items (user_id, record_key);

ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.shops ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.months ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pieces ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.days ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.individual_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.individual_entry_items ENABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE, DELETE ON
  public.user_profiles, public.shops, public.workers, public.months,
  public.pieces, public.days, public.entries, public.individual_entries,
  public.individual_entry_items
TO authenticated;

COMMENT ON TABLE public.user_profiles IS 'NoteBK cloud profile; one row per authenticated user.';
COMMENT ON TABLE public.shops IS 'NoteBK cloud shops. Rows are scoped by user_id and record_key.';
COMMENT ON TABLE public.entries IS 'NoteBK numeric ledger rows; unique per user, day legacy id and piece legacy id.';
COMMENT ON TABLE public.individual_entry_items IS 'NoteBK individual ledger quantities; unique per user, individual entry legacy id and piece legacy id.';
