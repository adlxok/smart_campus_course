/*
 Navicat Premium Data Transfer

 Source Server         : MySQL_Local
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : smart_campus_course

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 11/03/2026 23:48:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `parent_id` bigint NULL DEFAULT NULL,
  `reply_to_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (6, 2, 6, 'test5', '1232', '2026-03-10 15:29:59', NULL, NULL);
INSERT INTO `comment` VALUES (7, 2, 6, 'test5', '2131', '2026-03-10 15:30:01', NULL, NULL);
INSERT INTO `comment` VALUES (9, 2, 6, 'test5', '3123213', '2026-03-10 15:30:09', 8, 'test5');
INSERT INTO `comment` VALUES (10, 2, 6, 'test5', '123231', '2026-03-10 15:30:12', NULL, NULL);
INSERT INTO `comment` VALUES (12, 2, 6, 'test5', '123231', '2026-03-10 15:30:31', 8, 'test5');
INSERT INTO `comment` VALUES (13, 2, 6, 'test5', '2222222222', '2026-03-10 15:31:05', 8, 'test5');
INSERT INTO `comment` VALUES (14, 2, 6, 'test5', 'fdfdfdf', '2026-03-10 15:31:10', NULL, NULL);
INSERT INTO `comment` VALUES (15, 2, 6, 'test5', 'dffdfdfd', '2026-03-10 15:31:15', 8, 'test5');
INSERT INTO `comment` VALUES (17, 2, 5, 'test4', '纷纷', '2026-03-10 15:37:47', NULL, NULL);
INSERT INTO `comment` VALUES (18, 2, 5, 'test4', '额威风威风', '2026-03-10 15:37:51', NULL, NULL);
INSERT INTO `comment` VALUES (19, 2, 4, 'test3', 'sdasd', '2026-03-10 15:40:13', NULL, NULL);
INSERT INTO `comment` VALUES (20, 2, 6, 'test5', 'eqeqweqwe', '2026-03-10 16:55:36', NULL, NULL);
INSERT INTO `comment` VALUES (21, 2, 6, 'test5', 'qweqwe', '2026-03-10 16:55:38', NULL, NULL);
INSERT INTO `comment` VALUES (22, 2, 6, 'test5', 'qweqweqwe', '2026-03-10 16:55:40', NULL, NULL);
INSERT INTO `comment` VALUES (23, 2, 6, 'test5', 'eqwewqeqwe', '2026-03-10 16:55:42', NULL, NULL);
INSERT INTO `comment` VALUES (24, 2, 6, 'test5', 'fg', '2026-03-10 16:55:44', NULL, NULL);
INSERT INTO `comment` VALUES (25, 2, 6, 'test5', 'g', '2026-03-10 16:55:45', NULL, NULL);
INSERT INTO `comment` VALUES (26, 2, 6, 'test5', 'g', '2026-03-10 16:55:45', NULL, NULL);
INSERT INTO `comment` VALUES (27, 2, 6, 'test5', 'g', '2026-03-10 16:55:46', NULL, NULL);
INSERT INTO `comment` VALUES (28, 2, 6, 'test5', 'g', '2026-03-10 16:55:46', NULL, NULL);
INSERT INTO `comment` VALUES (29, 2, 6, 'test5', 'g', '2026-03-10 16:55:47', NULL, NULL);
INSERT INTO `comment` VALUES (30, 2, 6, 'test5', 'g', '2026-03-10 16:55:49', NULL, NULL);
INSERT INTO `comment` VALUES (31, 2, 6, 'test5', 'e12', '2026-03-10 16:55:56', 10, 'test5');
INSERT INTO `comment` VALUES (32, 2, 6, 'test5', '2', '2026-03-10 16:55:59', 31, 'test5');
INSERT INTO `comment` VALUES (33, 2, 6, 'test5', '3', '2026-03-10 16:56:03', 32, 'test5');
INSERT INTO `comment` VALUES (34, 2, 6, 'test5', '1', '2026-03-10 17:16:31', 31, 'test5');
INSERT INTO `comment` VALUES (35, 2, 6, 'test5', '1', '2026-03-10 17:16:37', 31, 'test5');
INSERT INTO `comment` VALUES (36, 2, 6, 'test5', '1', '2026-03-10 17:16:46', 7, 'test5');
INSERT INTO `comment` VALUES (37, 2, 5, 'test4', '1', '2026-03-10 17:17:07', 36, 'test5');
INSERT INTO `comment` VALUES (38, 2, 5, 'test4', '2', '2026-03-10 17:17:10', 36, 'test5');
INSERT INTO `comment` VALUES (39, 2, 5, 'test4', '1', '2026-03-10 19:24:07', NULL, NULL);
INSERT INTO `comment` VALUES (40, 2, 5, 'test4', '2', '2026-03-10 19:24:09', NULL, NULL);
INSERT INTO `comment` VALUES (41, 1, 1, 'test1', '这是一条测试评论', '2026-03-10 19:26:50', NULL, NULL);
INSERT INTO `comment` VALUES (42, 1, 2, 'test2', '这是第二条评论', '2026-03-10 19:26:50', NULL, NULL);
INSERT INTO `comment` VALUES (43, 1, 1, 'test1', '这是第三条评论', '2026-03-10 19:26:50', NULL, NULL);
INSERT INTO `comment` VALUES (44, 1, 2, 'test2', '第四条评论来了', '2026-03-10 19:26:50', NULL, NULL);
INSERT INTO `comment` VALUES (45, 1, 1, 'test1', '第五条评论', '2026-03-10 19:26:50', NULL, NULL);
INSERT INTO `comment` VALUES (46, 1, 2, 'test2', '第六条评论', '2026-03-10 19:26:50', NULL, NULL);
INSERT INTO `comment` VALUES (47, 2, 6, 'test5', '1', '2026-03-10 19:30:22', 18, 'test4');
INSERT INTO `comment` VALUES (48, 2, 6, 'test5', '1111', '2026-03-10 19:30:35', 18, 'test4');
INSERT INTO `comment` VALUES (49, 2, 6, 'test5', '石材城f', '2026-03-10 19:35:32', NULL, NULL);
INSERT INTO `comment` VALUES (50, 2, 6, 'test5', 'qweqwe', '2026-03-10 19:36:54', 49, 'test5');
INSERT INTO `comment` VALUES (51, 2, 6, 'test5', 'qwewqdsfdsfdsf', '2026-03-10 19:36:58', 49, 'test5');
INSERT INTO `comment` VALUES (52, 2, 6, 'test5', 'fsdfdsf', '2026-03-10 19:37:05', 49, 'test5');
INSERT INTO `comment` VALUES (53, 2, 6, 'test5', '3423423', '2026-03-10 19:37:09', NULL, NULL);
INSERT INTO `comment` VALUES (54, 2, 6, 'test5', '威威企鹅', '2026-03-10 19:55:10', 53, 'test5');
INSERT INTO `comment` VALUES (55, 2, 6, 'test5', '请问请问', '2026-03-10 19:55:22', 54, 'test5');
INSERT INTO `comment` VALUES (56, 2, 6, 'test5', '2323232', '2026-03-10 19:55:42', 55, 'test5');
INSERT INTO `comment` VALUES (57, 2, 5, 'test4', '这是一条测试评论', '2026-03-10 20:07:44', NULL, NULL);
INSERT INTO `comment` VALUES (59, 2, 6, 'test5', 'ewqe', '2026-03-10 20:09:35', 55, 'test5');
INSERT INTO `comment` VALUES (60, 2, 6, 'test5', '111', '2026-03-10 20:09:47', 54, 'test5');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'secret',
  `birthday` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'test0', '123456', 'USER', NULL, '2026-03-09 19:16:59', 'http://localhost:8080/backend/image/default_image/defaultImage.png', 'secret', NULL, NULL);
INSERT INTO `users` VALUES (2, 'test1', '$2a$10$qfcMQgrxiHryKf5NWuriN.yPezLCfOXU6kyLxLzsFeH.zhn2YdEUK', 'USER', NULL, '2026-03-09 19:16:58', 'http://localhost:8080/backend/image/default_image/defaultImage.png', 'secret', NULL, NULL);
INSERT INTO `users` VALUES (3, 'test2', '$2a$10$yQ/o6IpUCDLD.M44doOMnOCBjGHeQVz4pABLbA0.PMjiHSdHTxaDy', 'USER', '2026-03-09 18:46:14', '2026-03-09 19:16:56', 'http://localhost:8080/backend/image/default_image/defaultImage.png', 'secret', NULL, NULL);
INSERT INTO `users` VALUES (4, 'test3', '$2a$10$UBp53c0OKQE6wlR/aO.TXugBqnU7FHQ3TU.XiiYu57VIAGWP8dUfS', 'USER', '2026-03-09 19:23:20', '2026-03-09 19:23:20', NULL, 'secret', NULL, NULL);
INSERT INTO `users` VALUES (5, 'test4', '$2a$10$2JWuo0WMh0SdPMUc8QvY2.ZaedvjC.UDD5dUQEXHHnOCyv5N2BF/O', 'USER', '2026-03-09 19:36:00', '2026-03-09 19:36:00', 'http://localhost:8080/backend/image/5/1773238836251.jpg', 'male', '2026-03-02', 'errwerwe');
INSERT INTO `users` VALUES (6, 'test5', '$2a$10$ONvSPPxKZEMLqOny/6Wx/.zgSN3UzJhYEeHL9jOr1YFooJA8lKo0.', 'USER', '2026-03-09 19:41:11', '2026-03-09 19:41:11', 'http://localhost:8080/backend/image/6/1773057105732.jpg', 'secret', NULL, NULL);
INSERT INTO `users` VALUES (7, 'testuser', '$2a$10$0/Q9hV8IowWoRfSsWCgb9Oc5J./jxAfyMUzX.oljLE/fjQ9M.NQbu', 'USER', '2026-03-11 21:37:35', '2026-03-11 21:37:35', 'http://localhost:8080/backend/image/default_image/defaultImage.png', 'secret', NULL, NULL);

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `view_count` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video
-- ----------------------------
INSERT INTO `video` VALUES (1, '12313', '123123', 'http://localhost:8080/backend/video/6/1773110988556_e04228a5.mp4', 'http://localhost:8080/backend/cover/6/1773110988771_8e23ca8f.jpg', 6, 'test5', 33, '2026-03-10 10:49:49', '2026-03-11 23:46:27');
INSERT INTO `video` VALUES (2, '鹏爷', '大功可成', 'http://localhost:8080/backend/video/6/1773116152991_4f99db39.mp4', 'http://localhost:8080/backend/cover/6/1773116153202_f1de8b1e.jpg', 6, 'test5', 183, '2026-03-10 12:15:54', '2026-03-11 23:46:10');
INSERT INTO `video` VALUES (3, '埒', 'dasdad', 'http://localhost:8080/backend/video/5/1773241597443_b0eef312.mp4', 'http://localhost:8080/backend/cover/5/1773241597450_10a33ad2.jpg', 5, 'test4', 0, '2026-03-11 23:06:37', '2026-03-11 23:06:37');
INSERT INTO `video` VALUES (4, 'eqweqw', 'eqwewqe', 'http://localhost:8080/backend/video/5/1773241614507_ad4cfd53.mp4', 'http://localhost:8080/backend/cover/5/1773241614513_66ee0426.jpg', 5, 'test4', 0, '2026-03-11 23:06:55', '2026-03-11 23:06:55');
INSERT INTO `video` VALUES (5, 'qwewqe', 'qwewqe', 'http://localhost:8080/backend/video/5/1773241631515_59457b55.mp4', 'http://localhost:8080/backend/cover/5/1773241631520_cb5e12a5.jpg', 5, 'test4', 0, '2026-03-11 23:07:12', '2026-03-11 23:07:12');
INSERT INTO `video` VALUES (6, 'eweqw', 'qweqw', 'http://localhost:8080/backend/video/5/1773241641973_a52f692d.mp4', 'http://localhost:8080/backend/cover/5/1773241641978_ecae5d46.jpg', 5, 'test4', 0, '2026-03-11 23:07:22', '2026-03-11 23:07:22');
INSERT INTO `video` VALUES (7, 'qwewqe', 'qwewqe', 'http://localhost:8080/backend/video/5/1773241652570_d65a5386.mp4', 'http://localhost:8080/backend/cover/5/1773241652574_3efc1a66.jpg', 5, 'test4', 0, '2026-03-11 23:07:33', '2026-03-11 23:07:33');
INSERT INTO `video` VALUES (8, 'qwewqe', 'qweqwe', 'http://localhost:8080/backend/video/5/1773241666637_eb76ee66.mp4', 'http://localhost:8080/backend/cover/5/1773241666641_eebff1ff.jpg', 5, 'test4', 6, '2026-03-11 23:07:47', '2026-03-11 23:46:08');

-- ----------------------------
-- Table structure for video_favorite
-- ----------------------------
DROP TABLE IF EXISTS `video_favorite`;
CREATE TABLE `video_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_user`(`video_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_favorite
-- ----------------------------
INSERT INTO `video_favorite` VALUES (8, 2, 5, '2026-03-10 20:32:36');
INSERT INTO `video_favorite` VALUES (9, 1, 5, '2026-03-10 20:41:21');

-- ----------------------------
-- Table structure for video_like
-- ----------------------------
DROP TABLE IF EXISTS `video_like`;
CREATE TABLE `video_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_user`(`video_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_like
-- ----------------------------
INSERT INTO `video_like` VALUES (14, 2, 5, '2026-03-10 20:33:16');

SET FOREIGN_KEY_CHECKS = 1;
