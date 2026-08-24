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
        check (driving_status in ('driving', 'idle')),
    -- current position (nullable; the map uses it directly when present)
    lat             double precision,
    lng             double precision
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
 '{"type":"Polygon","coordinates":[[[126.9762,37.5682],[126.9778,37.5676],[126.9792,37.5683],[126.9806,37.5678],[126.9811,37.5668],[126.9805,37.5657],[126.9793,37.5651],[126.9776,37.5654],[126.9757,37.5664],[126.9762,37.5682]]]}'),
(1, 'Sector #2', 'Seoul Songpa-gu, Olympic-ro',
 '{"type":"Polygon","coordinates":[[[126.9810,37.5722],[126.9834,37.5724],[126.9852,37.5710],[126.9844,37.5690],[126.9814,37.5687],[126.9800,37.5703],[126.9810,37.5722]]]}');

insert into carts (sector_id, name, geofence_status, driving_status, lat, lng) values
-- Sector #1 (1 + 3 violating, rest compliant)
(1, 'Cart 1',  'violating', 'driving', 37.5690, 126.9820),
(1, 'Cart 2',  'compliant', 'driving', 37.5665, 126.9782),
(1, 'Cart 3',  'compliant', 'idle',    37.5670, 126.9790),
(1, 'Cart 6',  'violating', 'driving', 37.5688, 126.9820),
(1, 'Cart 7',  'violating', 'idle',    37.5648, 126.9748),
(1, 'Cart 8',  'violating', 'driving', 37.5695, 126.9800),
(1, 'Cart 9',  'compliant', 'driving', 37.5668, 126.9778),
(1, 'Cart 10', 'compliant', 'idle',    37.5672, 126.9785),
(1, 'Cart 11', 'compliant', 'driving', 37.5660, 126.9782),
(1, 'Cart 12', 'compliant', 'idle',    37.5665, 126.9772),
(1, 'Cart 13', 'compliant', 'driving', 37.5675, 126.9790),
(1, 'Cart 14', 'compliant', 'idle',    37.5658, 126.9788),
(1, 'Cart 15', 'compliant', 'driving', 37.5663, 126.9795),
(1, 'Cart 16', 'compliant', 'idle',    37.5669, 126.9768),
(1, 'Cart 17', 'compliant', 'driving', 37.5655, 126.9779),
(1, 'Cart 18', 'compliant', 'idle',    37.5678, 126.9783),
(1, 'Cart 19', 'compliant', 'driving', 37.5661, 126.9800),
(1, 'Cart 20', 'compliant', 'idle',    37.5673, 126.9776),
-- Sector #2
(2, 'Cart 4',  'compliant', 'driving', 37.5706, 126.9826),
(2, 'Cart 5',  'compliant', 'idle',    37.5700, 126.9820);

insert into geofence_events (cart_id, sector_id, occurred_at, location, max_speed, address) values
((select id from carts where name = 'Cart 1'), 1, now() - interval '5 minutes',
 '{"type":"Point","coordinates":[126.9820,37.5690]}', '16 Km/h', 'Seoul Jung-gu, Sejong-daero'),
((select id from carts where name = 'Cart 6'), 1, now() - interval '3 minutes',
 '{"type":"Point","coordinates":[126.9820,37.5688]}', '18 Km/h', 'Seoul Jung-gu, Toegye-ro'),
((select id from carts where name = 'Cart 7'), 1, now() - interval '12 minutes',
 '{"type":"Point","coordinates":[126.9748,37.5648]}', '21 Km/h', 'Seoul Jung-gu, Namdaemun-ro'),
((select id from carts where name = 'Cart 8'), 1, now() - interval '1 minute',
 '{"type":"Point","coordinates":[126.9800,37.5695]}', '15 Km/h', 'Seoul Jongno-gu, Jong-ro');
