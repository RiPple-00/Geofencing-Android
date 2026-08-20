-- Migration 005 — give Sector #1 an irregular (concave) geofence instead of the
-- clean pentagon, similar in style to the old mock sector3. Run ONCE on an
-- existing project. Cart positions are unchanged (compliant carts stay inside,
-- violating carts stay outside).

update sectors
set geofence = '{"type":"Polygon","coordinates":[[[126.9762,37.5682],[126.9778,37.5676],[126.9792,37.5683],[126.9806,37.5678],[126.9811,37.5668],[126.9805,37.5657],[126.9793,37.5651],[126.9776,37.5654],[126.9757,37.5664],[126.9762,37.5682]]]}'::jsonb
where name = 'Sector #1';
