-- 图片管理相关表结构

-- 文件信息表
CREATE TABLE `file_info` (
  `file_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `storage_path` varchar(500) NOT NULL COMMENT '实际存储路径',
  `access_url` varchar(500) DEFAULT NULL COMMENT '访问URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` bigint(20) DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`file_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_create_user` (`create_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 网站图片位置表
CREATE TABLE `web_image_position` (
  `position_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '位置ID',
  `web_id` varchar(50) NOT NULL COMMENT '网站ID',
  `position_code` varchar(100) NOT NULL COMMENT '位置编码',
  `position_name` varchar(200) NOT NULL COMMENT '位置名称',
  `file_id` bigint(20) DEFAULT NULL COMMENT '关联的文件ID',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `image_desc` varchar(500) DEFAULT NULL COMMENT '图片描述',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0禁用 1启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` bigint(20) DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`position_id`),
  UNIQUE KEY `uk_web_position` (`web_id`,`position_code`,`del_flag`),
  KEY `idx_web_id` (`web_id`),
  KEY `idx_position_code` (`position_code`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站图片位置表';
