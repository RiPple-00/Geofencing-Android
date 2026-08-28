-- ============================================================================
-- 008: extra mock data — Sector #3, #4 + carts + violation events
-- ----------------------------------------------------------------------------
-- Adds two more sectors (under the existing 'Golfzon County' site) with a
-- handful of carts each and a few recent violation events.
--
-- HOW TO USE: Supabase → SQL Editor → paste this whole file → Run.
-- Additive & safe to re-run for sectors/carts (on conflict do nothing);
-- geofence_events have no unique key, so run the events block only once.
--
-- Geo data: GeoJSON in jsonb. Polygon coords are [lng, lat] and the ring is
-- closed (first point == last point). Points are [lng, lat].
-- Violating carts sit OUTSIDE the geofence, compliant carts INSIDE.
-- ============================================================================

-- ── Sectors ──────────────────────────────────────────────────────────────────
insert into sectors (site_id, name, address, geofence) values
((select id from sites where name = 'Golfzon County'),
 'Sector #3', 'Seoul Gangnam-gu, Teheran-ro',
 '{"type":"Polygon","coordinates":[[[127.0290,37.5055],[127.0365,37.5060],[127.0385,37.5015],[127.0345,37.4985],[127.0285,37.4995],[127.0275,37.5030],[127.0290,37.5055]]]}'),
((select id from sites where name = 'Golfzon County'),
 'Sector #4', 'Seoul Yeongdeungpo-gu, Gukhoe-daero',
 '{"type":"Polygon","coordinates":[[[126.9185,37.5305],[126.9265,37.5310],[126.9295,37.5265],[126.9250,37.5230],[126.9190,37.5245],[126.9170,37.5280],[126.9185,37.5305]]]}')
on conflict do nothing;

-- ── Carts: Sector #3 (2 violating outside, 8 compliant inside) ────────────────
insert into carts (sector_id, name, geofence_status, driving_status, lat, lng)
select s.id, c.name, c.geofence_status, c.driving_status, c.lat, c.lng
from sectors s
join (values
    ('Cart 1',  'violating', 'driving', 37.5085, 127.0350),
    ('Cart 2',  'compliant', 'driving', 37.5030, 127.0325),
    ('Cart 3',  'compliant', 'idle',    37.5018, 127.0340),
    ('Cart 4',  'violating', 'idle',    37.5015, 127.0415),
    ('Cart 5',  'compliant', 'driving', 37.5042, 127.0308),
    ('Cart 6',  'compliant', 'idle',    37.5025, 127.0352),
    ('Cart 7',  'compliant', 'driving', 37.5008, 127.0322),
    ('Cart 8',  'compliant', 'idle',    37.5038, 127.0335),
    ('Cart 9',  'compliant', 'driving', 37.5022, 127.0300),
    ('Cart 10', 'compliant', 'idle',    37.5048, 127.0330)
) as c(name, geofence_status, driving_status, lat, lng) on true
where s.name = 'Sector #3'
on conflict do nothing;

-- ── Carts: Sector #4 (2 violating outside, 6 compliant inside) ────────────────
insert into carts (sector_id, name, geofence_status, driving_status, lat, lng)
select s.id, c.name, c.geofence_status, c.driving_status, c.lat, c.lng
from sectors s
join (values
    ('Cart 1', 'violating', 'driving', 37.5340, 126.9250),
    ('Cart 2', 'compliant', 'driving', 37.5278, 126.9240),
    ('Cart 3', 'compliant', 'idle',    37.5262, 126.9262),
    ('Cart 4', 'violating', 'idle',    37.5258, 126.9335),
    ('Cart 5', 'compliant', 'driving', 37.5288, 126.9225),
    ('Cart 6', 'compliant', 'idle',    37.5272, 126.9252),
    ('Cart 7', 'compliant', 'driving', 37.5266, 126.9272),
    ('Cart 8', 'compliant', 'idle',    37.5282, 126.9235)
) as c(name, geofence_status, driving_status, lat, lng) on true
where s.name = 'Sector #4'
on conflict do nothing;

-- ── Violation events (run once — no unique key) ───────────────────────────────
-- Cart is matched within its sector (names repeat across sectors).
insert into geofence_events (cart_id, sector_id, occurred_at, location, max_speed, address)
select c.id, c.sector_id, e.occurred_at, e.location::jsonb, e.max_speed, e.address
from (values
    ('Sector #3', 'Cart 1', now() - interval '4 minutes',
     '{"type":"Point","coordinates":[127.0350,37.5085]}', '17 Km/h', 'Seoul Gangnam-gu, Teheran-ro'),
    ('Sector #3', 'Cart 4', now() - interval '9 minutes',
     '{"type":"Point","coordinates":[127.0415,37.5015]}', '22 Km/h', 'Seoul Gangnam-gu, Yeoksam-ro'),
    ('Sector #4', 'Cart 1', now() - interval '2 minutes',
     '{"type":"Point","coordinates":[126.9250,37.5340]}', '19 Km/h', 'Seoul Yeongdeungpo-gu, Gukhoe-daero'),
    ('Sector #4', 'Cart 4', now() - interval '7 minutes',
     '{"type":"Point","coordinates":[126.9335,37.5258]}', '15 Km/h', 'Seoul Yeongdeungpo-gu, Uisadang-daero')
) as e(sector_name, cart_name, occurred_at, location, max_speed, address)
join sectors s on s.name = e.sector_name
join carts   c on c.sector_id = s.id and c.name = e.cart_name;
