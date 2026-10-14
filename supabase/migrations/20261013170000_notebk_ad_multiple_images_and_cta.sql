alter table public.notebk_ads
  add column if not exists image_urls jsonb not null default '[]'::jsonb,
  add column if not exists button_text text not null default 'معرفة المزيد';

update public.notebk_ads
set image_urls = case
  when coalesce(image_url, '') <> '' then jsonb_build_array(image_url)
  else '[]'::jsonb
end
where image_urls = '[]'::jsonb and coalesce(image_url, '') <> '';

alter table public.notebk_ads
  drop constraint if exists notebk_ads_image_urls_is_array;
alter table public.notebk_ads
  add constraint notebk_ads_image_urls_is_array
  check (jsonb_typeof(image_urls) = 'array');
