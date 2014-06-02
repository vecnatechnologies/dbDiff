create table person (
  id int8 not null primary key,
  name varchar(255) not null,
  dob timestamp not null
);

create index name_dob_idx on person (name, dob);

create table person_relatives (
  person_id int8 not null,
  relative_id int8 not null,
  relationship varchar(255),
  primary key (person_id, relative_id)
);

alter table person_relatives add constraint fk_person foreign key (person_id) references person (id);
alter table person_relatives add constraint fk_relative foreign key (relative_id) references person (id);