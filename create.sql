create table sample_points(
	point_id uuid primary key,
	geom geometry,
	time integer not null,
	SMC text not null,
	DMC text not null,
	XMC text not null,
	YMC text not null,
	CMC text not null,
	
	pH double precision not null,
	OC double precision not null,
	N double precision not null,
	P double precision not null,
	K double precision not null,
	
	distance double precision not null
);

select * from sample_points;
select * from points_3857_10km;

insert into sample_points
	select 
		uuid_generate_v4() as point_id,
		geom as geom,
		time::integer as time,
		SMC,
		DMC,
		XMC,
		YMC,
		CMC,
	
		pH,
		有机质 as OC,
		碱解氮 as N,
		有效磷 as P,
		速效钾 as K,
	
		distance
	from points_3857_10km;

select *, ST_AsText(geom) as point from sample_points order by random();

select ST_AsText(geom) as point, N, * from sample_points where point_id = '8a23b556-7a75-4da8-b19a-152fb4c8dbe9';

select *, ST_AsText(geom) as point from sample_points
	where point_id != '8a23b556-7a75-4da8-b19a-152fb4c8dbe9' and 
	distance <= 5000 and N > 1 and N <= 250 order by random();