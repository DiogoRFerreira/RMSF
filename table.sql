drop table if exists Person;
drop table if exists Person_PAN;
drop table if exists PAN;
drop table if exists Node;
drop table if exists NodeReadings;

create table Person(
	E-mail varchar(255),
	Name varchar(255),
	Password varchar(255),
    Notify bit,
    primary key(E-mail)
);
   
create table Person_PAN( 
    PersonE-mail varchar(255),
	PanidPAN integer(10),
    Enable bit,
    primary key(PersonE-mail,PANidPan),
    foreign key(PersonE-mail) references Person,
	foreign key(PANidPAN) references PAN
);
    
create table PAN(
	idPAN integer(10),
	Serial_key varchar(255),
    primary key(idPAN)
);
    
create table Node(
	idNode integer(10),
	Activated bit,
	PANidPAN integer(10),
    primary key(idPAN),
    foreign key(PSNidPAN) references (PAN)
);

create table NodeReadings(
	TimeStamp timestamp,
	NodeidNode integer(10),
    primary key(TimeStamp),
    foreign key(NodeidNode) references (Node)
);

insert into Person values("diogo.r.ferreira@tecnico.ulisboa.pt", "Diogo Ferreira", "1234",0);
insert into Person values("jose.manuel.dias@tecnico.ulisboa.pt", "José Dias", "1234",0);

insert into Person_PAN values("diogo.r.ferreira@tecnico.ulisboa.pt",12345,1);
insert into Person_PAN values("jose.manuel.dias@tecnico.ulisboa.pt",12345,1);

insert into PAN values(12345,"AbC356eF");

insert into Node values(1,0,12345);
insert into Node values(2,0,12345);
insert into Node values(3,0,12345);

insert into NodeReadings values ('2001-01-01 01:01:01', 1);
insert into NodeReadings values ('2001-01-01 01:01:01', 2);
insert into NodeReadings values ('2001-01-01 01:01:01', 3);
