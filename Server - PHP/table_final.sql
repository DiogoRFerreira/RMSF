drop table if exists NodeReadings;
drop table if exists Node;
drop table if exists PersonPAN;
drop table if exists Person;
drop table if exists PAN;

create table Person(
	Email varchar(255),
	Name varchar(255),
	Password varchar(255),
    Notify boolean,
    primary key(Email)
);

create table PAN(
	idPAN numeric(10),
	Serial_key varchar(255),
    primary key(idPAN)
);
   
create table PersonPAN( 
    Email varchar(255),
	idPAN numeric(10),
    Enable boolean,
    primary key(Email,idPan),
    foreign key(Email) references Person(Email),
	foreign key(idPAN) references PAN(idPAN)
);
    
create table Node(
	idNode numeric(10),
	Activated boolean,
	idPAN numeric(10),
    primary key(idPAN,idNode),
    foreign key(idPAN) references PAN(idPAN)
);

create table NodeReadings(
	TStamp timestamp,
	idNode numeric(10),
	idPAN numeric(10),
    primary key(TStamp,idNode, idPAN),
    foreign key(idPAN,idNode) references Node(idPAN,idNode)
);

insert into Person values('diogo.r.ferreira@tecnico.ulisboa.pt', 'Diogo Ferreira', '1234',false);
insert into Person values('jose.manuel.dias@tecnico.ulisboa.pt', 'Jose Dias', '1234',false);

insert into PAN values(12345,'AbC356eF');

insert into PersonPAN values('diogo.r.ferreira@tecnico.ulisboa.pt',12345,true);
insert into PersonPAN values('jose.manuel.dias@tecnico.ulisboa.pt',12345,true);

insert into Node values(1,false,12345);
insert into Node values(2,false,12345);
insert into Node values(3,false,12345);

insert into NodeReadings values ('2001-01-01 01:01:01', 1,12345);
insert into NodeReadings values ('2001-01-01 01:01:01', 2,12345);
insert into NodeReadings values ('2001-01-01 01:01:01', 3,12345);
