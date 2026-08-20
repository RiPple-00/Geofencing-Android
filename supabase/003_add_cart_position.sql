-- Migration 003 — add current position (lat/lng) to carts so the map shows
-- real positions instead of synthesized ones. Run ONCE on an existing project.

alter table carts add column if not exists lat double precision;
alter table carts add column if not exists lng double precision;

-- backfill seeded carts (cart 1 is violating → placed outside its geofence)
update carts set lat = 37.5690, lng = 126.9820 where id = 1;
update carts set lat = 37.5665, lng = 126.9782 where id = 2;
update carts set lat = 37.5670, lng = 126.9790 where id = 3;
update carts set lat = 37.5706, lng = 126.9826 where id = 4;
update carts set lat = 37.5700, lng = 126.9820 where id = 5;
