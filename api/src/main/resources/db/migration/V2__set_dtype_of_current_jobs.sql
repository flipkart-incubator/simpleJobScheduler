
alter table job add column dtype varchar(31) not NULL ;

update job set dtype="scheduledJob";
