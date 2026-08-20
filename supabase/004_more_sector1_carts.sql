-- Migration 004 — shorten cart names and add 15 more carts to Sector #1.
-- Run ONCE on an existing project.

-- 1) shorten existing names ("Golfzon County N" -> "Cart N")
update carts set name = 'Cart ' || id where name like 'Golfzon County %';

-- 2) 15 more carts in Sector #1 (id 1): 3 violating (outside geofence), 12 compliant (inside)
insert into carts (sector_id, name, geofence_status, driving_status, lat, lng) values
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
(1, 'Cart 20', 'compliant', 'idle',    37.5673, 126.9776);

-- 3) violation events (with address) for the new violating carts
insert into geofence_events (cart_id, sector_id, occurred_at, location, max_speed, address) values
((select id from carts where name = 'Cart 6'), 1, now() - interval '3 minutes',
 '{"type":"Point","coordinates":[126.9820,37.5688]}', '18 Km/h', 'Seoul Jung-gu, Toegye-ro'),
((select id from carts where name = 'Cart 7'), 1, now() - interval '12 minutes',
 '{"type":"Point","coordinates":[126.9748,37.5648]}', '21 Km/h', 'Seoul Jung-gu, Namdaemun-ro'),
((select id from carts where name = 'Cart 8'), 1, now() - interval '1 minute',
 '{"type":"Point","coordinates":[126.9800,37.5695]}', '15 Km/h', 'Seoul Jongno-gu, Jong-ro');
