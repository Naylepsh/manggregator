CREATE TABLE IF NOT EXISTS "schema_migrations" (version varchar(128) primary key);
CREATE TABLE asset (
  id uuid primary key,
  name text not null,
  enabled integer not null
);
CREATE TABLE chapter (
  id uuid primary key,
  no text not null,
  url text not null,
  dateReleased text not null,
  assetId uuid not null, seen integer not null default 0,

  foreign key (assetId) references asset (id)
);
CREATE TABLE chapters_page (
  id uuid primary key,
  assetId uuid not null,
  site text not null,
  url text not null,

  foreign key (assetId) references asset (id)
);
-- Dbmate schema migrations
INSERT INTO "schema_migrations" (version) VALUES
  ('20221129182103'),
  ('20230212125338');
