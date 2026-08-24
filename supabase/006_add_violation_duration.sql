-- Migration 006 — store the violation duration as a backend value on
-- geofence_events (instead of the app computing now - occurred_at). Run ONCE.

alter table geofence_events add column if not exists duration text;

update geofence_events set duration = '5m 12s'  where cart_id = (select id from carts where name = 'Cart 1');
update geofence_events set duration = '3m 04s'  where cart_id = (select id from carts where name = 'Cart 6');
update geofence_events set duration = '12m 30s' where cart_id = (select id from carts where name = 'Cart 7');
update geofence_events set duration = '1m 08s'  where cart_id = (select id from carts where name = 'Cart 8');
