-- 用户访问统计相关表结构

-- 用户访问记录表
CREATE TABLE `user_visit_record` (
  `visit_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `web_id` varchar(50) NOT NULL COMMENT '网站ID',
  `page_url` varchar(500) DEFAULT NULL COMMENT '页面URL',
  `page_title` varchar(200) DEFAULT NULL COMMENT '页面标题',
  `visit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  `leave_time` datetime DEFAULT NULL COMMENT '离开时间',
  `duration` int(11) DEFAULT '0' COMMENT '停留时长(秒)',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型',
  `browser` varchar(50) DEFAULT NULL COMMENT '浏览器',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`visit_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_web_id` (`web_id`),
  KEY `idx_visit_time` (`visit_time`),
  KEY `idx_duration` (`duration`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户访问记录表';

-- 用户每日统计表
CREATE TABLE `user_daily_stats` (
  `stats_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `web_id` varchar(50) NOT NULL COMMENT '网站ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `visit_count` int(11) DEFAULT '0' COMMENT '访问次数',
  `total_duration` int(11) DEFAULT '0' COMMENT '总停留时长(秒)',
  `avg_duration` int(11) DEFAULT '0' COMMENT '平均停留时长(秒)',
  `page_count` int(11) DEFAULT '0' COMMENT '访问页面数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stats_id`),
  UNIQUE KEY `uk_user_web_date` (`user_id`,`web_id`,`stats_date`),
  KEY `idx_stats_date` (`stats_date`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日统计表';

-- 网站访问汇总统计表
CREATE TABLE `web_visit_summary` (
  `summary_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '汇总ID',
  `web_id` varchar(50) NOT NULL COMMENT '网站ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `total_visits` int(11) DEFAULT '0' COMMENT '总访问量',
  `unique_users` int(11) DEFAULT '0' COMMENT '独立访客数',
  `total_duration` int(11) DEFAULT '0' COMMENT '总停留时长(秒)',
  `avg_duration` int(11) DEFAULT '0' COMMENT '平均停留时长(秒)',
  `bounce_rate` decimal(5,2) DEFAULT '0.00' COMMENT '跳出率(%)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`summary_id`),
  UNIQUE KEY `uk_web_date` (`web_id`,`stats_date`),
  KEY `idx_stats_date` (`stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站访问汇总统计表';