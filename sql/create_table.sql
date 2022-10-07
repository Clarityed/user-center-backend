-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)      null comment '用户昵称',
    userAccount  varchar(256)      null comment '账号',
    avatarUrl    varchar(1024)     null comment '用户头像',
    gender       tinyint           null comment '性别',
    userPassword varchar(512)      not null comment '密码',
    userProfile  varchar(512)      null comment '个人简介',
    email        varchar(512)      null comment '邮箱',
    userStatus   int     default 0 not null comment '状态 0 -正常',
    createTime   datetime          null comment '创建时间',
    updateTime   datetime          null comment '更新时间',
    isDelete     tinyint default 0 not null comment '是否删除 0 -正常 1 -删除',
    userRole     int     default 0 not null comment '用户角色 0 -普通用户 1 -管理员',
    userCode     varchar(512)      null comment '用户编号',
    tags         varchar(1024)     null comment '标签 json 列表',
    phone        varchar(128)      null comment '电话'
)
    comment '用户表' charset = utf8;

create table team
(
    id           bigint auto_increment comment 'id'
        primary key,
    name   		varchar(256)                   not null comment '队伍名称',
    description varchar(1024)                  null 	comment '描述',
    maxNum    	int      default 1             not null comment '最大人数',
    expireTime  datetime  					   null 	comment '过期时间',
    userId      bigint 						   not null		comment '用户id（队长 id）',
    status    	int      default 0             not null comment '0 - 公开，1 - 私有，2 - 加密',
    password varchar(512)                      null 	comment '密码',
    createTime   datetime                      null comment '创建时间',
    updateTime   datetime                      null comment '更新时间',
    isDelete     tinyint  default 0            not null comment '是否删除'
)
    comment '队伍' charset = utf8;

create table user_team
(
    id           bigint auto_increment comment 'id'
        primary key,
    userId       bigint                not null comment '用户id',
    teamId       bigint                not null comment '队伍id',
    joinTime     datetime              null comment '加入时间',
    createTime   datetime              null comment '创建时间',
    updateTime   datetime              null comment '更新时间',
    isDelete     tinyint  default 0    not null comment '是否删除'
)
    comment '用户队伍关系' charset = utf8;

-- auto-generated definition
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)      null comment '标签名称',
    userId     bigint            null comment '用户 id',
    parentId   bigint            null comment '父标签 id',
    isParent   tinyint           null comment '0 - 不是，1 - 父标签',
    createTime datetime          null comment '创建时间',
    updateTime datetime          null comment '更新时间',
    isDelete   tinyint default 0 not null comment '是否删除 0 -正常 1 -删除',
    constraint uniIdx_tagName
        unique (tagName)
)
    comment '标签表';

create index idx_userId
    on tag (userId);

