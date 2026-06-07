drop database `bbs`;
CREATE DATABASE IF NOT EXISTS `bbs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `bbs`;

-- -----------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
                        `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
                        `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                        `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                        `work_place` varchar(100) DEFAULT NULL COMMENT '工作地点',
                        `job_nature` varchar(50) DEFAULT NULL COMMENT '工作性质',
                        `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
                        `role` tinyint NOT NULL DEFAULT '0' COMMENT '0普通用户 1管理员',
                        `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1正常 2禁用',
                        `score` int NOT NULL DEFAULT '0' COMMENT '可用积分',
                        `frozen_score` int NOT NULL DEFAULT '0' COMMENT '冻结积分',
                        `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
                        `login_count` int NOT NULL DEFAULT '0' COMMENT '登录次数',
                        `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
                        `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删除 1已删除',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 板块表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `board`;
CREATE TABLE `board` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `name` varchar(100) NOT NULL COMMENT '板块名称',
                         `description` varchar(500) DEFAULT NULL COMMENT '板块描述',
                         `icon` varchar(255) DEFAULT NULL COMMENT '图标',
                         `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序顺序',
                         `post_count` int NOT NULL DEFAULT '0' COMMENT '帖子数',
                         `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1正常',
                         `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删除 1已删除',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- -----------------------------------------------------------
-- 3. 帖子表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `board_id` bigint NOT NULL COMMENT '所属板块ID',
                        `user_id` bigint NOT NULL COMMENT '作者ID',
                        `title` varchar(200) NOT NULL COMMENT '标题',
                        `content` longtext NOT NULL COMMENT '内容（富文本HTML）',
                        `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
                        `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数',
                        `is_top` tinyint NOT NULL DEFAULT '0' COMMENT '0普通 1板块置顶 2全局置顶',
                        `is_essence` tinyint NOT NULL DEFAULT '0' COMMENT '0否 1精华',
                        `status` tinyint NOT NULL DEFAULT '1' COMMENT '0待审核 1正常 2违规下架',
                        `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删除 1已删除',
                        `last_reply_time` datetime DEFAULT NULL COMMENT '最后回复时间',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        KEY `idx_board_id` (`board_id`),
                        KEY `idx_user_id` (`user_id`),
                        KEY `idx_status_top_time` (`status`,`is_top`,`create_time`),
    -- 外键约束：帖子属于一个板块
                        CONSTRAINT `fk_post_board` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键约束：帖子属于一个用户
                        CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- -----------------------------------------------------------
-- 4. 回复表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `reply`;
CREATE TABLE `reply` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `post_id` bigint NOT NULL COMMENT '帖子ID',
                         `user_id` bigint NOT NULL COMMENT '回复者ID',
                         `parent_id` bigint DEFAULT NULL COMMENT '楼中楼父回复ID，NULL为一级回复',
                         `floor` int NOT NULL COMMENT '楼层号',
                         `content` text NOT NULL COMMENT '回复内容',
                         `status` tinyint NOT NULL DEFAULT '1' COMMENT '0待审核 1正常',
                         `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删除 1已删除',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         KEY `idx_post_id` (`post_id`),
                         KEY `idx_parent_id` (`parent_id`),
    -- 外键约束：回复属于一个帖子
                         CONSTRAINT `fk_reply_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键约束：回复属于一个用户
                         CONSTRAINT `fk_reply_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键约束：楼中楼回复属于一个父回复
                         CONSTRAINT `fk_reply_parent` FOREIGN KEY (`parent_id`) REFERENCES `reply` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复表';

-- -----------------------------------------------------------
-- 5. 需求悬赏表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `demand`;
CREATE TABLE `demand` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `user_id` bigint NOT NULL COMMENT '发布者ID',
                          `title` varchar(200) NOT NULL COMMENT '标题',
                          `content` longtext NOT NULL COMMENT '内容',
                          `score` int NOT NULL DEFAULT '0' COMMENT '悬赏积分',
                          `status` tinyint NOT NULL DEFAULT '0' COMMENT '0进行中 1已解决 2已关闭',
                          `resolver_id` bigint DEFAULT NULL COMMENT '解决者ID',
                          `adopt_reply_id` bigint DEFAULT NULL COMMENT '采纳的回复ID',
                          `resolve_time` datetime DEFAULT NULL COMMENT '解决时间',
                          `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删除 1已删除',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          KEY `idx_user_id` (`user_id`),
                          KEY `idx_status` (`status`),
    -- 外键约束：需求属于一个发布者
                          CONSTRAINT `fk_demand_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键约束：需求有一个解决者
                          CONSTRAINT `fk_demand_resolver` FOREIGN KEY (`resolver_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求悬赏表';

-- -----------------------------------------------------------
-- 6. 需求回复表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `demand_reply`;
CREATE TABLE `demand_reply` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `demand_id` bigint NOT NULL COMMENT '需求ID',
                                `user_id` bigint NOT NULL COMMENT '回复者ID',
                                `content` text NOT NULL COMMENT '回复内容',
                                `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
                                `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删除 1已删除',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_demand_id` (`demand_id`),
    -- 外键约束：需求回复属于一个需求
                                CONSTRAINT `fk_demand_reply_demand` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键约束：需求回复属于一个用户
                                CONSTRAINT `fk_demand_reply_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求回复表';

-- 补充外键：需求可以采纳一个回复（因为demand_reply表在demand之后创建，所以单独添加）
ALTER TABLE `demand`
    ADD CONSTRAINT `fk_demand_adopt_reply` FOREIGN KEY (`adopt_reply_id`) REFERENCES `demand_reply` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- -----------------------------------------------------------
-- 7. 积分记录表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `score_record`;
CREATE TABLE `score_record` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `type` tinyint NOT NULL COMMENT '1获得 2支出 3冻结 4解冻',
                                `amount` int NOT NULL COMMENT '变动数量（正数）',
                                `balance` int NOT NULL COMMENT '变动后余额',
                                `source_type` varchar(50) DEFAULT NULL COMMENT '来源类型 demand/post/system',
                                `source_id` bigint DEFAULT NULL COMMENT '来源ID',
                                `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_user_id_type` (`user_id`,`type`),
    -- 外键约束：积分记录属于一个用户
                                CONSTRAINT `fk_score_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- -----------------------------------------------------------
-- 8. 管理员操作日志表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `admin_id` bigint NOT NULL COMMENT '管理员ID',
                             `action` varchar(100) NOT NULL COMMENT '操作描述',
                             `target_type` varchar(50) NOT NULL COMMENT '对象类型 user/post/board/demand',
                             `target_id` bigint NOT NULL COMMENT '对象ID',
                             `old_value` varchar(500) DEFAULT NULL COMMENT '旧值',
                             `new_value` varchar(500) DEFAULT NULL COMMENT '新值',
                             `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             KEY `idx_admin_id` (`admin_id`),
                             KEY `idx_target` (`target_type`,`target_id`),
    -- 外键约束：操作日志属于一个管理员
                             CONSTRAINT `fk_admin_log_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- ============================================================
-- 初始数据（完全保留原版）
-- ============================================================

-- 管理员：admin / admin123
INSERT INTO `user` (`id`,`username`,`password`,`real_name`,`phone`,`email`,`role`,`status`,`score`,`deleted`) VALUES
    (1,'admin','$2b$04$WhHB/vMbF5KYgSOPZ/GFB.9OqsD70.iqW39vtFOz7nZMAoBk0W1dK','管理员','13800138000','admin@bbs.com',1,1,1000,0);

-- 普通用户：testuser / 123456
INSERT INTO `user` (`id`,`username`,`password`,`real_name`,`phone`,`email`,`role`,`status`,`score`,`deleted`) VALUES
    (2,'testuser','$2b$04$WhHB/vMbF5KYgSOPZ/GFB.9OqsD70.iqW39vtFOz7nZMAoBk0W1dK','测试用户','13900139000','test@bbs.com',0,1,500,0);

-- 待审核用户：newuser / 123456
INSERT INTO `user` (`id`,`username`,`password`,`real_name`,`phone`,`email`,`role`,`status`,`score`,`deleted`) VALUES
    (3,'newuser','$2b$04$WhHB/vMbF5KYgSOPZ/GFB.9OqsD70.iqW39vtFOz7nZMAoBk0W1dK','新用户','13700137000','new@bbs.com',0,0,100,0);

-- 板块数据
INSERT INTO `board` (`id`,`name`,`description`,`sort_order`,`status`,`deleted`) VALUES
                                                                                    (1,'技术交流','编程、算法、框架等技术讨论',1,1,0),
                                                                                    (2,'校园生活','课程、食堂、宿舍、活动',2,1,0),
                                                                                    (3,'二手交易','闲置物品、书籍、电子产品',3,1,0),
                                                                                    (4,'求职招聘','实习、校招、内推信息',4,1,0);

-- 注意：BCrypt 密码哈希值在上方 INSERT 中已使用固定值
-- $2b$04$WhHB/vMbF5KYgSOPZ/GFB.9OqsD70.iqW39vtFOz7nZMAoBk0W1dK 对应密码 "123456"
-- 管理员密码 "admin123" 在后续 Phase 中会使用 BCrypt 编码写入，当前 INSERT 中已使用相同哈希便于测试