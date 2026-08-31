-- ============================================================================
-- Geofencing — interim Supabase (Postgres) backend  ·  consolidated schema + seed
-- ----------------------------------------------------------------------------
-- Stands in for the team REST BE until it is ready. Mirrors the REST spec's
-- shape so the app maps it the same way.
--
-- Geo data is stored as GeoJSON in `jsonb` (matching the spec verbatim) instead
-- of PostGIS geometry — no spatial queries are needed for this interim setup,
-- and PostgREST returns the jsonb as-is.
--
-- This is the SINGLE source of truth: tables already include every column and
-- constraint, and the seed already contains all 4 sectors and their carts/events
-- (the old incremental migrations 002–009 are folded in here).
--
-- HOW TO USE: create a Supabase project → SQL Editor → paste this whole file →
-- Run. Designed for a FRESH project (identity ids start at 1, so sectors get
-- ids 1..4 in insert order and the seed references them directly).
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
    geofence jsonb  not null,
    -- 앱이 sector를 이름으로 조회하므로(사이트 내) 이름 유일성을 보장한다.
    unique (site_id, name)
);

create table if not exists carts (
    id              bigint generated always as identity primary key,
    sector_id       bigint not null references sectors (id) on delete cascade,
    name            text   not null,
    -- 'disconnected' = 마지막 위치를 유지한 채 통신이 끊긴 상태(앱의 Disconnect 섹션).
    geofence_status text   not null default 'compliant'
        check (geofence_status in ('violating', 'compliant', 'disconnected')),
    driving_status  text   not null default 'idle'
        check (driving_status in ('driving', 'idle')),
    -- current position (nullable; the map uses it directly when present)
    lat             double precision,
    lng             double precision,
    -- 앱이 cart를 (섹터 내) 이름으로 식별하므로 이름 유일성을 보장한다.
    unique (sector_id, name)
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
    s.id                                                          as sector_id,
    count(c.*)                                                    as total,
    count(c.*) filter (where c.geofence_status = 'violating')     as violating,
    count(c.*) filter (where c.geofence_status = 'compliant')     as compliant,
    count(c.*) filter (where c.geofence_status = 'disconnected')  as disconnected,
    count(c.*) filter (where c.driving_status = 'driving')        as driving,
    count(c.*) filter (where c.driving_status = 'idle')           as idle
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

-- ============================================================================
-- Seed data — one site, four sectors (Seoul), carts, and recent events.
-- Violating carts sit OUTSIDE their geofence; compliant/disconnected INSIDE.
-- ============================================================================

insert into sites (name) values ('Golfzon County');

-- Sectors 1..4 (fresh project → these get ids 1, 2, 3, 4 in this order).
insert into sectors (site_id, name, address, geofence) values
(1, 'Sector #1', 'Seoul Jung-gu, Sejong-daero',
 '{"type":"Polygon","coordinates":[[[126.9762,37.5682],[126.9778,37.5676],[126.9792,37.5683],[126.9806,37.5678],[126.9811,37.5668],[126.9805,37.5657],[126.9793,37.5651],[126.9776,37.5654],[126.9757,37.5664],[126.9762,37.5682]]]}'),
(1, 'Sector #2', 'Seoul Songpa-gu, Olympic-ro',
 '{"type":"Polygon","coordinates":[[[126.9810,37.5722],[126.9834,37.5724],[126.9852,37.5710],[126.9844,37.5690],[126.9814,37.5687],[126.9800,37.5703],[126.9810,37.5722]]]}'),
(1, 'Sector #3', 'Seoul Gangnam-gu, Teheran-ro',
 '{"type":"Polygon","coordinates":[[[127.0290,37.5055],[127.0365,37.5060],[127.0385,37.5015],[127.0345,37.4985],[127.0285,37.4995],[127.0275,37.5030],[127.0290,37.5055]]]}'),
(1, 'Sector #4', 'Seoul Yeongdeungpo-gu, Gukhoe-daero',
 '{"type":"Polygon","coordinates":[[[126.9185,37.5305],[126.9265,37.5310],[126.9295,37.5265],[126.9250,37.5230],[126.9190,37.5245],[126.9170,37.5280],[126.9185,37.5305]]]}');

-- Carts. sector_id references the sectors above (1..4).
insert into carts (sector_id, name, geofence_status, driving_status, lat, lng) values
-- Sector #1 (4 violating outside, rest compliant inside)
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
(2, 'Cart 5',  'compliant', 'idle',    37.5700, 126.9820),
-- Sector #3 (2 violating outside, 8 compliant inside, 2 disconnected inside)
(3, 'Cart 1',  'violating',    'driving', 37.5085, 127.0350),
(3, 'Cart 2',  'compliant',    'driving', 37.5030, 127.0325),
(3, 'Cart 3',  'compliant',    'idle',    37.5018, 127.0340),
(3, 'Cart 4',  'violating',    'idle',    37.5015, 127.0415),
(3, 'Cart 5',  'compliant',    'driving', 37.5042, 127.0308),
(3, 'Cart 6',  'compliant',    'idle',    37.5025, 127.0352),
(3, 'Cart 7',  'compliant',    'driving', 37.5008, 127.0322),
(3, 'Cart 8',  'compliant',    'idle',    37.5038, 127.0335),
(3, 'Cart 9',  'compliant',    'driving', 37.5022, 127.0300),
(3, 'Cart 10', 'compliant',    'idle',    37.5048, 127.0330),
(3, 'Cart 11', 'disconnected', 'idle',    37.5028, 127.0330),
(3, 'Cart 12', 'disconnected', 'idle',    37.5012, 127.0345),
-- Sector #4 (2 violating outside, 6 compliant inside, 1 disconnected inside)
(4, 'Cart 1',  'violating',    'driving', 37.5340, 126.9250),
(4, 'Cart 2',  'compliant',    'driving', 37.5278, 126.9240),
(4, 'Cart 3',  'compliant',    'idle',    37.5262, 126.9262),
(4, 'Cart 4',  'violating',    'idle',    37.5258, 126.9335),
(4, 'Cart 5',  'compliant',    'driving', 37.5288, 126.9225),
(4, 'Cart 6',  'compliant',    'idle',    37.5272, 126.9252),
(4, 'Cart 7',  'compliant',    'driving', 37.5266, 126.9272),
(4, 'Cart 8',  'compliant',    'idle',    37.5282, 126.9235),
(4, 'Cart 9',  'disconnected', 'idle',    37.5268, 126.9255);

-- Recent violation events. Cart is matched within its sector (names repeat
-- across sectors), so scope each lookup by sector_id.
insert into geofence_events (cart_id, sector_id, occurred_at, location, max_speed, address) values
-- Sector #1
((select id from carts where sector_id = 1 and name = 'Cart 1'), 1, now() - interval '5 minutes',
 '{"type":"Point","coordinates":[126.9820,37.5690]}', '16 Km/h', 'Seoul Jung-gu, Sejong-daero'),
((select id from carts where sector_id = 1 and name = 'Cart 6'), 1, now() - interval '3 minutes',
 '{"type":"Point","coordinates":[126.9820,37.5688]}', '18 Km/h', 'Seoul Jung-gu, Toegye-ro'),
((select id from carts where sector_id = 1 and name = 'Cart 7'), 1, now() - interval '12 minutes',
 '{"type":"Point","coordinates":[126.9748,37.5648]}', '21 Km/h', 'Seoul Jung-gu, Namdaemun-ro'),
((select id from carts where sector_id = 1 and name = 'Cart 8'), 1, now() - interval '1 minute',
 '{"type":"Point","coordinates":[126.9800,37.5695]}', '15 Km/h', 'Seoul Jongno-gu, Jong-ro'),
-- Sector #3
((select id from carts where sector_id = 3 and name = 'Cart 1'), 3, now() - interval '4 minutes',
 '{"type":"Point","coordinates":[127.0350,37.5085]}', '17 Km/h', 'Seoul Gangnam-gu, Teheran-ro'),
((select id from carts where sector_id = 3 and name = 'Cart 4'), 3, now() - interval '9 minutes',
 '{"type":"Point","coordinates":[127.0415,37.5015]}', '22 Km/h', 'Seoul Gangnam-gu, Yeoksam-ro'),
-- Sector #4
((select id from carts where sector_id = 4 and name = 'Cart 1'), 4, now() - interval '2 minutes',
 '{"type":"Point","coordinates":[126.9250,37.5340]}', '19 Km/h', 'Seoul Yeongdeungpo-gu, Gukhoe-daero'),
((select id from carts where sector_id = 4 and name = 'Cart 4'), 4, now() - interval '7 minutes',
 '{"type":"Point","coordinates":[126.9335,37.5258]}', '15 Km/h', 'Seoul Yeongdeungpo-gu, Uisadang-daero');
