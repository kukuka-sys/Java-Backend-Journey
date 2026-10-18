CREATE DATABASE IF NOT EXISTS `dianping_practice`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `dianping_practice`;

CREATE TABLE IF NOT EXISTS `tb_user` (
    `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `nick_name` VARCHAR(64) NOT NULL COMMENT '昵称',
    `login_name` VARCHAR(64) NOT NULL COMMENT '登录名',
    `introduce_sign` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0 正常，1 删除',
    `locked_flag` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0 正常，1 锁定',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_tb_user_login_name` (`login_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
