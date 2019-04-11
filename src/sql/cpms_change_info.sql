/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50562
Source Host           : localhost:3306
Source Database       : cpms

Target Server Type    : MYSQL
Target Server Version : 50562
File Encoding         : 65001

Date: 2019-04-11 23:30:34
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cpms_change_info
-- ----------------------------
DROP TABLE IF EXISTS `cpms_change_info`;
CREATE TABLE `cpms_change_info` (
  `id` varchar(64) NOT NULL DEFAULT '' COMMENT '主键',
  `company_name` varchar(64) DEFAULT NULL COMMENT '企业名称',
  `project_name` varchar(64) DEFAULT NULL COMMENT '变更项目',
  `change_date` datetime DEFAULT NULL COMMENT '变更日期',
  `before_info` varchar(500) DEFAULT NULL COMMENT '变更前信息',
  `after_info` varchar(500) DEFAULT NULL COMMENT '变更后信息',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='企业信息变更记录';
SET FOREIGN_KEY_CHECKS=1;
