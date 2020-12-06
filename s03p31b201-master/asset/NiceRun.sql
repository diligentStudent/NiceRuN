use pjt3;

#user 정보를 저장하는 테이블  
create table user(
	#유저가 사는 위치를 저장
	location varchar(255),
    
    #칼로리 소모를 계산하기 위한 키, 몸무게, 성별
    height int not null, 
    weight int not null,
    gender boolean not null,
    
    #목표거리만큼 뛰었는지 확인 하기 위한 목표 설정
    goaldist int not null,
    
    #유저id로 사용하기 위한 이메일 주소
    email varchar(255) not null primary key,
    #비밀번호
    password varchar(255) not null,
    #성
    lastname varchar(255) not null,
    #이름
    firstname varchar(255) not null,
    
    #프로필 사진
    profileimg varchar(255) default null,
    
    #생년월일
    birthday date
);

create table track(
	trackid varchar(255) primary key,
    #JSON파일 이름
    filename varchar(255) not null,
    dist double not null,
    trackimg varchar(255) not null
);

create table mytrack(
	mytrackid int auto_increment primary key,
    email varchar(255),
    trackid varchar(255),
    snsdownload boolean default false,
    #공유된 시간
    sharedtime datetime default now(),
    foreign key(email) references user(email) on delete cascade on update cascade,
    foreign key(trackid) references track(trackid) 
);

create table trackinfo(
	trackinfoid int auto_increment primary key,
	mytrackid int,
    #달리기를 시작한 날짜
    createat datetime not null,
	#달리기를 종료한 날짜
    endat datetime default now(),
    #속도 칼로리 실재로 달린거리
    speed double not null,
    kcal int not null,
    dist double not null,
      
    foreign key(mytrackid) references mytrack(mytrackid)
);

create table calendar(
 calendarid int auto_increment primary key,
 email varchar(255),
 issucceeded boolean,
 #오늘 뛴거리
 dist double,
 #현재 목표치
 goaldist int,
 today datetime default now(),
 foreign key(email) references user(email) on delete cascade on update cascade
);

create table sns(
mytrackid int primary key,
createdat datetime default now(),
comment varchar(255),
foreign key(mytrackid) references mytrack(mytrackid) on delete cascade on update cascade
);