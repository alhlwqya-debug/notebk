insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'notebk-advertisements',
  'notebk-advertisements',
  true,
  5242880,
  array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update
set public = true,
    file_size_limit = 5242880,
    allowed_mime_types = array['image/jpeg', 'image/png', 'image/webp'];

drop policy if exists "Public can view NoteBK advertisement images" on storage.objects;
create policy "Public can view NoteBK advertisement images"
on storage.objects for select
to anon, authenticated
using (bucket_id = 'notebk-advertisements');

drop policy if exists "NoteBK admins can upload advertisement images" on storage.objects;
create policy "NoteBK admins can upload advertisement images"
on storage.objects for insert
to authenticated
with check (
  bucket_id = 'notebk-advertisements'
  and (select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin'
);

drop policy if exists "NoteBK admins can update advertisement images" on storage.objects;
create policy "NoteBK admins can update advertisement images"
on storage.objects for update
to authenticated
using (
  bucket_id = 'notebk-advertisements'
  and (select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin'
)
with check (
  bucket_id = 'notebk-advertisements'
  and (select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin'
);

drop policy if exists "NoteBK admins can delete advertisement images" on storage.objects;
create policy "NoteBK admins can delete advertisement images"
on storage.objects for delete
to authenticated
using (
  bucket_id = 'notebk-advertisements'
  and (select auth.jwt() -> 'app_metadata' ->> 'role') = 'notebk_admin'
);
