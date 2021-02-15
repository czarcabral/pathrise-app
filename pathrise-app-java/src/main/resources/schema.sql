drop table if exists `job`;
create table `job` (
	`id` int auto_increment primary key,
    `job_title` varchar(255),
    `company_name` varchar(255),
    `job_url` varchar(10000),
    `job_source` varchar(255)
);
