-- migrate:up
create table asset (
  id uuid primary key,
  name text not null,
  enabled integer not null
);

create table chapter (
  id uuid primary key,
  no text not null,
  url text not null,
  dateReleased text not null,
  assetId uuid not null,

  foreign key (assetId) references asset (id)
);

create table chapters_page (
  id uuid primary key,
  assetId uuid not null,
  site text not null,
  page text not null,

  foreign key (assetId) references asset (id)
);

-- migrate:down
drop table if exists chapters_page;
drop table if exists chapter;
drop table if exists asset;