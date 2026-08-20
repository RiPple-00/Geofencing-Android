-- Migration 002 — add violation-detail columns to geofence_events.
-- Run ONCE on an existing project (schema.sql already includes these for fresh setups).
-- Re-running schema.sql on an existing DB would duplicate the seed, so use this instead.

alter table geofence_events add column if not exists max_speed text;
alter table geofence_events add column if not exists address   text;

-- backfill the seeded violation event
update geofence_events
set max_speed = '16 Km/h',
    address   = 'Seoul Jung-gu, Sejong-daero'
where cart_id = 1;
