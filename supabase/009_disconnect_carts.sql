-- ============================================================================
-- 009: allow 'disconnected' geofence_status + add a few disconnect carts
-- ----------------------------------------------------------------------------
-- The app maps geofence_status 'disconnected' -> Disconnect. The schema only
-- allowed 'violating'/'compliant', so widen the check first, then add a few
-- disconnect carts (position = last known, inside the geofence) so the
-- Disconnect section/badge can be verified.
--
-- HOW TO USE: Supabase → SQL Editor → paste this whole file → Run.
-- Requires sectors #3/#4 from 008 (adjust names if you use different sectors).
-- ============================================================================

-- ── Widen geofence_status check to include 'disconnected' ─────────────────────
alter table carts drop constraint if exists carts_geofence_status_check;
alter table carts add constraint carts_geofence_status_check
    check (geofence_status in ('violating', 'compliant', 'disconnected'));

-- ── Add disconnect carts ──────────────────────────────────────────────────────
insert into carts (sector_id, name, geofence_status, driving_status, lat, lng)
select s.id, c.name, 'disconnected', c.driving_status, c.lat, c.lng
from (values
    ('Sector #3', 'Cart 11', 'idle', 37.5028, 127.0330),
    ('Sector #3', 'Cart 12', 'idle', 37.5012, 127.0345),
    ('Sector #4', 'Cart 9',  'idle', 37.5268, 126.9255)
) as c(sector_name, name, driving_status, lat, lng)
join sectors s on s.name = c.sector_name
on conflict do nothing;
