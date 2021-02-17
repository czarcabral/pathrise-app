drop table if exists job;
create table job (
	id SERIAL primary key,
    job_title varchar(255),
    company_name varchar(255),
    job_url varchar(10000),
    job_source varchar(255)
);
drop table if exists job_board;
create table job_board (
	id SERIAL primary key,
    name varchar(255),
    rating varchar(255),
    root_domain varchar(255),
    logo_file varchar(1000),
    description varchar(2000)
);
