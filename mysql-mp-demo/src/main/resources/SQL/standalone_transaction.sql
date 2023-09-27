/*
Navicat MySQL Data Transfer

Source Server         : 华为云_mysql
Source Server Version : 80027
Source Host           : 121.36.70.23:3306
Source Database       : standalone_transaction

Target Server Type    : MYSQL
Target Server Version : 80027
File Encoding         : 65001

Date: 2023-09-27 17:30:04
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `money` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES ('1', '张三', '2000');
INSERT INTO `account` VALUES ('2', '李四', '2000');
