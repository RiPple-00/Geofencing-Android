-- Migration 007 — enforce the name uniqueness the app relies on.
-- The app looks sectors/carts up by name (SupabaseDashboardRepository), so
-- duplicate names could surface the wrong row. Run ONCE on an existing project
-- (existing seed names are already unique, so these apply cleanly).

alter table sectors add constraint sectors_site_name_unique unique (site_id, name);
alter table carts   add constraint carts_sector_name_unique unique (sector_id, name);
