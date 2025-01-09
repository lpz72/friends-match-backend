create table if not exists `user-center`.user
(
    id           bigint auto_increment comment 'id'
    primary key,
    username     varchar(256)                       null comment '�û��ǳ�',
    userAccount  varchar(256)                       null comment '��¼�˺�',
    avatarUrl    varchar(1024)                      null comment 'ͷ��',
    userPassword varchar(256)                       null comment '����',
    gender       tinyint                            null comment '�Ա�',
    phone        varchar(256)                       null comment '�绰',
    email        varchar(256)                       null comment '����',
    userStatus   int      default 0                 not null comment '�û�״̬ 0:����',
    createTime   datetime default CURRENT_TIMESTAMP null comment '����ʱ��',
    updateTime   datetime default CURRENT_TIMESTAMP null comment '����ʱ��',
    isDelete     tinyint  default 0                 not null comment '�߼�ɾ��',
    userRole     int      default 0                 not null comment '�û���ɫ 0����ͨ�û� 1������Ա',
    planetCode   varchar(512)                       null comment '������'
    );