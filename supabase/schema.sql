-- ============================================================================
-- Geofencing — interim Supabase (Postgres) backend
-- ----------------------------------------------------------------------------
-- Stands in for the team REST BE until it is ready. Mirrors the REST spec's
-- shape so the app maps it the same way.
--
-- Geo data is stored as GeoJSON in `jsonb` (matching the spec verbatim) instead
-- of PostGIS geometry — no spatial queries are needed for this interim setup,
-- and PostgREST returns the jsonb as-is.
--
-- HOW TO USE: create a Supabase project → SQL Editor → paste this whole file →
-- Run. Designed for a FRESH project (identity ids start at 1).
-- ============================================================================

-- ── Tables ──────────────────────────────────────────────────────────────────

create table if not exists sites (
    id   bigint generated always as identity primary key,
    name text not null
);

create table if not exists sectors (
    id       bigint generated always as identity primary key,
    site_id  bigint not null references sites (id) on delete cascade,
    name     text   not null,
    address  text   not null,
    -- GeoJSON Polygon: { "type":"Polygon", "coordinates":[[[lng,lat], ...]] }
    -- Note: coordinates are [lng, lat] (lng first) and the ring is closed
    -- (first point == last point), per GeoJSON (RFC 7946).
    geofence jsonb  not null
);

create table if not exists carts (
    id              bigint generated always as identity primary key,
    sector_id       bigint not null references sectors (id) on delete cascade,
    name            text   not null,
    geofence_status text   not null default 'compliant'
        check (geofence_status in ('violating', 'compliant')),
    driving_status  text   not null default 'idle'
        check (driving_status in ('driving', 'idle'))
);

create table if not exists geofence_events (
    id          bigint      generated always as identity primary key,
    cart_id     bigint      not null references carts (id) on delete cascade,
    sector_id   bigint      not null references sectors (id) on delete cascade,
    occurred_at timestamptz not null default now(),
    -- GeoJSON Point: { "type":"Point", "coordinates":[lng,lat] }
    location    jsonb       not null,
    -- violation detail (nullable; shown on the cart detail screen)
    max_speed   text,
    address     text
);

-- speeds up the "recent events per sector" lookup
create index if not exists idx_geofence_events_sector_time
    on geofence_events (sector_id, occurred_at desc);

-- ── Convenience view — per-sector cart counts ────────────────────────────────
-- Lets the app read pre-aggregated counts with a single select instead of
-- pulling every cart and counting on the client.
-- security_invoker = on → the view respects the underlying tables' RLS policies.
create or replace view sector_cart_counts
with (security_invoker = on) as
select
    s.id                                                        as sector_id,
    count(c.*)                                                  as total,
    count(c.*) filter (where c.geofence_status = 'violating')   as violating,
    count(c.*) filter (where c.geofence_status = 'compliant')   as compliant,
    count(c.*) filter (where c.driving_status = 'driving')      as driving,
    count(c.*) filter (where c.driving_status = 'idle')         as idle
from sectors s
left join carts c on c.sector_id = s.id
group by s.id;

-- ── Row Level Security ───────────────────────────────────────────────────────
-- Supabase enables RLS by default; without a policy the anon key reads nothing.
-- This interim data is non-sensitive, so allow public READ only (no writes).
alter table sites           enable row level security;
alter table sectors         enable row level security;
alter table carts           enable row level security;
alter table geofence_events enable row level security;

create policy "public read sites"    on sites           for select using (true);
create policy "public read sectors"  on sectors         for select using (true);
create policy "public read carts"    on carts           for select using (true);
create policy "public read events"   on geofence_events for select using (true);

-- ── Seed data ────────────────────────────────────────────────────────────────
-- One site, two sectors (Seoul), a handful of carts, and one recent event.

insert into sites (name) values ('Golfzon County');

insert into sectors (site_id, name, address, geofence) values
(1, 'Sector #1', 'Seoul Jung-gu, Sejong-daero',
 '{"type":"Polygon","coordinates":[[[126.9758,37.5683],[126.9804,37.5681],[126.9810,37.5661],[126.9788,37.5646],[126.9756,37.5653],[126.9758,37.5683]]]}'),
(1, 'Sector #2', 'Seoul Songpa-gu, Olympic-ro',
 '{"type":"Polygon","coordinates":[[[126.9810,37.5722],[126.9834,37.5724],[126.9852,37.5710],[126.9844,37.5690],[126.9814,37.5687],[126.9800,37.5703],[126.9810,37.5722]]]}');

insert into carts (sector_id, name, geofence_status, driving_status) values
(1, 'Golfzon County 1', 'violating', 'driving'),
(1, 'Golfzon County 2', 'compliant', 'driving'),
(1, 'Golfzon County 3', 'compliant', 'idle'),
(2, 'Golfzon County 4', 'compliant', 'driving'),
(2, 'Golfzon County 5', 'compliant', 'idle');

insert into geofence_events (cart_id, sector_id, occurred_at, location, max_speed, address) values
(1, 1, now() - interval '5 minutes',
 '{"type":"Point","coordinates":[126.9820,37.5690]}', '16 Km/h', 'Seoul Jung-gu, Sejong-daero');
