create table if not exists public.notebk_ads (
  id uuid primary key default gen_random_uuid(),
  title text not null check (char_length(trim(title)) between 1 and 120),
  body text not null default '' check (char_length(body) <= 1000),
  image_url text not null default '',
  target_url text not null default '',
  is_active boolean not null default true,
  sort_order integer not null default 0,
  starts_at timestamptz,
  ends_at timestamptz,
  created_by uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint notebk_ads_schedule_valid check (starts_at is null or ends_at is null or ends_at > starts_at)
);

create index if not exists notebk_ads_active_schedule_idx
  on public.notebk_ads (is_active, sort_order, created_at desc);

create or replace function public.notebk_ads_set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists notebk_ads_updated_at on public.notebk_ads;
create trigger notebk_ads_updated_at
before update on public.notebk_ads
for each row execute function public.notebk_ads_set_updated_at();

alter table public.notebk_ads enable row level security;

revoke all on table public.notebk_ads from anon, authenticated;
grant select on table public.notebk_ads to anon, authenticated;
grant insert, update, delete on table public.notebk_ads to authenticated;

drop policy if exists "Public can read currently active NoteBK ads" on public.notebk_ads;
create policy "Public can read currently active NoteBK ads"
on public.notebk_ads for select
to anon, authenticated
using (
  is_active = true
  and (starts_at is null or starts_at <= now())
  and (ends_at is null or ends_at > now())
);

drop policy if exists "NoteBK admins can read all ads" on public.notebk_ads;
create policy "NoteBK admins can read all ads"
on public.notebk_ads for select
to authenticated
using ((select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin');

drop policy if exists "NoteBK admins can create ads" on public.notebk_ads;
create policy "NoteBK admins can create ads"
on public.notebk_ads for insert
to authenticated
with check (
  (select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin'
  and (created_by is null or created_by = (select auth.uid()))
);

drop policy if exists "NoteBK admins can update ads" on public.notebk_ads;
create policy "NoteBK admins can update ads"
on public.notebk_ads for update
to authenticated
using ((select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin')
with check ((select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin');

drop policy if exists "NoteBK admins can delete ads" on public.notebk_ads;
create policy "NoteBK admins can delete ads"
on public.notebk_ads for delete
to authenticated
using ((select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin');
