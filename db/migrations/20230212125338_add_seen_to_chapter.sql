-- migrate:up
alter table chapter
  add seen integer not null default 0;

-- migrate:down
alter table chapter
  drop column seen;
