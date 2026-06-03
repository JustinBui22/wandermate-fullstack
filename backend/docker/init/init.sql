/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.8.7-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: local/demo    Database: traveling_app_demo
-- ------------------------------------------------------
-- Server version	10.7.4-MariaDB-1:10.7.4+maria~focal

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `accommodations`
--

DROP TABLE IF EXISTS `accommodations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `accommodations` (
  `accommodation_id` int(11) NOT NULL AUTO_INCREMENT,
  `accommodation_name` varchar(255) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`accommodation_id`),
  UNIQUE KEY `UKpoqt6c2inlbditbx8j9861i8n` (`accommodation_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accommodations`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `accommodations` WRITE;
/*!40000 ALTER TABLE `accommodations` DISABLE KEYS */;
/*!40000 ALTER TABLE `accommodations` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `activities`
--

DROP TABLE IF EXISTS `activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `activities` (
  `activity_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_name` varchar(255) NOT NULL,
  `created_date` datetime(6) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `end_date_time` datetime(6) NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `start_date_time` datetime(6) NOT NULL,
  `trip_id` bigint(20) NOT NULL,
  PRIMARY KEY (`activity_id`),
  KEY `idx_activities_trip_id` (`trip_id`),
  KEY `idx_activities_start_date_time` (`start_date_time`),
  CONSTRAINT `FKkx78ofjocchsrmnsf02creh65` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activities`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `activities` WRITE;
/*!40000 ALTER TABLE `activities` DISABLE KEYS */;
/*!40000 ALTER TABLE `activities` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `cities`
--

DROP TABLE IF EXISTS `cities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cities` (
  `city_id` int(11) NOT NULL AUTO_INCREMENT,
  `city_name` varchar(255) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`city_id`),
  UNIQUE KEY `UKrlmpoah07xxtfr03pmosd593p` (`city_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cities`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `cities` WRITE;
/*!40000 ALTER TABLE `cities` DISABLE KEYS */;
/*!40000 ALTER TABLE `cities` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration` (
  `config_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `config_code` varchar(255) NOT NULL,
  `config_message` varchar(255) DEFAULT NULL,
  `config_type` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `config_value` varchar(10000) NOT NULL,
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuration`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `configuration` WRITE;
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` VALUES
(1,'2024-12-04 00:00:00.000000','PASSWORD_PATTERN','Password must be 8-20 characters long, include at least one lowercase letter, one uppercase letter, one digit, one special character, and must not contain whitespace, <, >, /, or \\ characters.',NULL,NULL,'^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:\'\",.<>?])[^\\s<>\\\\/]{8,20}$'),
(2,'2024-12-04 00:00:00.000000','EMAIL_PATTERN','Config for email pattern',NULL,NULL,'^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
(3,'2024-12-05 00:00:00.000000','PHONE_VN_PATTERN','Phone numbers must start with either +84, 84, or 0 (03, 05, 07, 08, 09)',NULL,NULL,'^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$'),
(4,'2024-12-08 00:00:00.000000','USERNAME_PATTERN','Username must have 5-20 characters, no backspace, no tab, and do not match phone pattern ',NULL,NULL,'^(?!^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$)(?!.*[\\u0008\\t])[a-zA-Z0-9_.-]{5,20}$'),
(5,'2024-12-12 00:00:00.000000','ACCESS_TOKEN_EXPIRATION_TIME','Config for API authenticated jwt token expiration time in ms',NULL,NULL,'3600000'),
(6,'2024-12-12 00:00:00.000000','SECRET_KEY_CONFIG','Config for secret key for API token (Minimum 256-bit for HS512)',NULL,NULL,'your-secure-secret-key-here-your-secure-secret-key-here'),
(7,'2024-12-14 00:00:00.000000','NON_AUTHENTICATED_REQUEST','Config for all API request that do not need authentication',NULL,NULL,'/api/v1/users/register,\r\n/api/v1/users/login,\r\n/api/v1/auth/refresh,\r\n/api/v1/users/forgot-password,\r\n/api/v1/users/register/verify,\r\n/api/v1/otp/send,\r\n/api/v1/otp/verify,\r\n/api/v1/users/check,\r\n/swagger-ui/**,\r\n/swagger-ui.html,\r\n/v3/api-docs/**,\r\n/v3/api-docs.yaml,\r\n/v3/api-docs'),
(9,'2025-01-05 00:00:00.000000','MAX_ALLOWED_SESSIONS','Config for the maximum number of allowed sessions.',NULL,NULL,'3'),
(10,'2025-01-09 00:00:00.000000','OTP_EXPIRATION_TIME','Config for OTP expiration time in ms',NULL,NULL,'300000'),
(11,'2025-01-10 00:00:00.000000','EMAIL_ADDRESS_CONFIG','Config for email sender',NULL,NULL,'demo@example.com'),
(12,'2025-01-10 00:00:00.000000','EMAIL_ACCESS_TOKEN_CONFIG','Config for email access token',NULL,NULL,'replace_me'),
(13,'2025-01-10 00:00:00.000000','EMAIL_HOST_CONFIG','Config for email host',NULL,NULL,'smtp.gmail.com'),
(14,'2025-01-10 00:00:00.000000','EMAIL_PORT_CONFIG','Config for email port',NULL,NULL,'587'),
(15,'2025-01-11 00:00:00.000000','EMAIL_CLIENT_ID','Config for google client id',NULL,NULL,'replace_me'),
(16,'2025-01-11 00:00:00.000000','EMAIL_CLIENT_SECRET','Config for google client secret',NULL,NULL,'replace_me'),
(17,'2025-01-11 00:00:00.000000','EMAIL_TOKEN_URL','Config for google token url',NULL,NULL,'https://oauth2.googleapis.com/token'),
(18,'2025-01-11 00:00:00.000000','EMAIL_REFRESH_TOKEN','Config for google refresh token',NULL,NULL,'replace_me'),
(19,'2025-01-11 00:00:00.000000','EMAIL_REFRESH_ACCESS_TOKEN_RATE','Config for google refreshing rate of access token',NULL,NULL,'3500000'),
(20,'2025-01-14 00:00:00.000000','OTP_RESTRICTED_TIME','Config for OTP restricted time for failing otp verification too much',NULL,NULL,'900000'),
(21,'2025-01-15 00:00:00.000000','MAX_RETRY_OTP','Config for max number of retry OTP',NULL,NULL,'3'),
(22,'2025-07-01 00:00:00.000000','REFRESH_TOKEN_EXPIRATION_TIME','Config for expiration date for refresh token in month',NULL,NULL,'1'),
(23,'2026-04-22 21:27:02.000000','MIN_SUGGEST_CHARACTER','Config for minimum num of charaters to get suggestions in searching',NULL,NULL,'2'),
(24,NOW(),'EMAIL_OAUTH_REFRESH_ENABLED','Enable or disable scheduled email OAuth token refresh','EMAIL',NULL,'false');
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `email_contents`
--

DROP TABLE IF EXISTS `email_contents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_contents` (
  `email_id` int(11) NOT NULL AUTO_INCREMENT,
  `email_code` varchar(255) NOT NULL,
  `email_content` text NOT NULL,
  `email_flow` varchar(255) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `email_enum` varchar(100) NOT NULL,
  `email_subject` varchar(255) NOT NULL,
  PRIMARY KEY (`email_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_contents`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `email_contents` WRITE;
/*!40000 ALTER TABLE `email_contents` DISABLE KEYS */;
INSERT INTO `email_contents` VALUES
(1,'EMS01','Hello <b>{name}</b>,  \n\n<p>For security purposes, please use the following One-Time Password (OTP) to access your account:</p>  \n\n<h2 style=\"color: #2c3e50;\">{otp}</h2>  \n\n<p>This code was requested from the <b>Travelling App</b> and will remain valid for <b>{expire_time}</b> minutes.</p>  \n\n<p>If you did not request this OTP, please ignore this email. Do not share this code with anyone for security reasons.</p>  \n\n<p>Safe travels!</p>  \n\nBest regards,  \n<p><b>The Travelling App Team</b></p>\n','REGISTER','2025-01-09 00:00:00.000000',NULL,'EMAIL_OTP_REGISTER','Here\'s your One Time Password (OTP) - Expire in {expire_time} minutes!');
/*!40000 ALTER TABLE `email_contents` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `error_codes`
--

DROP TABLE IF EXISTS `error_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `error_codes` (
  `error_id` int(11) NOT NULL AUTO_INCREMENT,
  `flow` varchar(255) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `error_code` varchar(255) NOT NULL,
  `error_message` varchar(255) DEFAULT NULL,
  `error_description` varchar(255) DEFAULT NULL,
  `error_type` varchar(255) DEFAULT NULL,
  `error_enum` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `createdd_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`error_id`),
  UNIQUE KEY `UKsdw4mfc7rh54yl4shlr4ukpi3` (`error_code`,`flow`,`error_enum`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `error_codes`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `error_codes` WRITE;
/*!40000 ALTER TABLE `error_codes` DISABLE KEYS */;
INSERT INTO `error_codes` VALUES
(1,'COMMON','2024-12-03','E001','Invalid input provided',NULL,NULL,'INVALID_INPUT',NULL,NULL),
(2,'REGISTER','2024-12-03','E002','Username has already been taken',NULL,NULL,'PASSWORD_UPDATED_SUCCESS',NULL,NULL),
(3,'REGISTER','2024-12-03','E003','Email has already been taken',NULL,NULL,'EMAIL_TAKEN',NULL,NULL),
(4,'REGISTER','2024-12-03','E004','Password must be 8-20 characters long, include at least one lowercase letter, one uppercase letter, one digit, one special character, and must not contain whitespace, <, >, /, or \\ characters.',NULL,NULL,'PASSWORD_NOT_QUALIFIED',NULL,NULL),
(5,'COMMON','2024-12-03','E005','User not found',NULL,NULL,'USER_NOT_FOUND',NULL,NULL),
(6,'COMMON','2024-12-03','E006','Client internal server error',NULL,NULL,'CLIENT_SERVER_ERROR',NULL,NULL),
(7,'LOGIN','2024-12-03','E007','Password does not match',NULL,NULL,'PASSWORD_NOT_CORRECT',NULL,NULL),
(8,'COMMON','2024-12-03','E008','Undefined error code',NULL,NULL,'UNDEFINED_ERROR_CODE',NULL,NULL),
(9,'COMMON','2024-12-03','E009','Undefined http status code',NULL,NULL,'UNDEFINED_HTTP_CODE',NULL,NULL),
(10,'REGISTER','2024-12-03','E000','User has been created successfully',NULL,NULL,'USER_CREATED',NULL,NULL),
(11,'REGISTER','2024-12-04','E010','Email format is invalid',NULL,NULL,'EMAIL_PATTERN_INVALID',NULL,NULL),
(12,'REGISTER','2024-12-05','E011','Phone number must start with 0, 84, or +84, followed by 3, 5, 7, 8, or 9, and contain 7 to 8 digits',NULL,NULL,'PHONE_FORMAT_INVALID',NULL,NULL),
(13,'SMS','2024-12-08','E012','SMS config is not found',NULL,NULL,'SMS_NOT_CONFIG',NULL,NULL),
(14,'REGISTER','2024-12-08','E013','Username format invalid','Username must have 5-20 characters, no backspace, no tab, and do not match phone pattern ',NULL,'USERNAME_FORMAT_INVALID',NULL,NULL),
(28,'LOGIN','2024-12-08','E000','User login successfully',NULL,NULL,'LOGIN_SUCCESS',NULL,NULL),
(29,'TOKEN','2024-12-12','E000','Token generate successfully',NULL,NULL,'TOKEN_GENERATE_SUCCESS',NULL,NULL),
(30,'TOKEN','2024-12-12','E014','Token generate fail',NULL,NULL,'TOKEN_GENERATE_FAIL',NULL,NULL),
(31,'TOKEN','2024-12-12','E015','Token verify fail',NULL,NULL,'TOKEN_VERIFY_FAIL',NULL,NULL),
(32,'TOKEN','2024-12-12','E000','Token verified successfully',NULL,NULL,'TOKEN_VERIFY_SUCCESS',NULL,NULL),
(33,'TOKEN','2024-12-12','E016','Session expires',NULL,NULL,'TOKEN_EXPIRE',NULL,NULL),
(34,'COMMON','2024-12-17','E017','Internal server error',NULL,NULL,'INTERNAL_SERVER_ERROR',NULL,NULL),
(35,'COMMON','2024-12-17','E018','Config not found',NULL,NULL,'CONFIG_NOT_FOUND',NULL,NULL),
(36,'COMMON','2024-12-21','E019','Input format invalid',NULL,NULL,'INPUT_FORMAT_INVALID',NULL,NULL),
(37,'COMMON','2024-12-21','E020','OTP code verification fail',NULL,NULL,'OTP_VERIFICATION_FAIL',NULL,NULL),
(38,'TOKEN','2025-01-03','E021','Token not found',NULL,NULL,'TOKEN_NOT_FOUND',NULL,NULL),
(39,'TOKEN','2025-01-03','E000','Token retrieved successfully',NULL,NULL,'TOKEN_RETRIEVE_SUCCESS',NULL,NULL),
(40,'LOGIN','2025-01-06','E022','Max session reached',NULL,NULL,'MAX_SESSIONS_REACHED',NULL,NULL),
(42,'TOKEN','2025-01-07','E023','Session token invalid',NULL,NULL,'SESSION_TOKEN_INVALID',NULL,NULL),
(43,'OTP','2025-01-08','E000','OTP code verification successfully',NULL,NULL,'OTP_VERIFICATION_SUCCESS',NULL,NULL),
(44,'OTP','2025-01-08','E000','Otp created successfully',NULL,NULL,'OTP_CREATED_SUCCESS',NULL,NULL),
(45,'OTP','2025-01-08','E000','Otp sent successfully',NULL,NULL,'OTP_SENT_SUCCESS',NULL,NULL),
(46,'SMS','2025-01-08','E000','Sms sent successfully',NULL,NULL,'SMS_SENT_SUCCESS',NULL,NULL),
(47,'SMS','2025-01-08','E024','Sms sent failed',NULL,NULL,'SMS_SENT_FAIL',NULL,NULL),
(48,'EMAIL','2025-01-10','E000','Email sent successfully',NULL,NULL,'EMAIL_SENT_SUCCESS',NULL,NULL),
(49,'EMAIL','2025-01-10','E025','Email sent failed',NULL,NULL,'EMAIL_SENT_FAIL',NULL,NULL),
(50,'OTP','2025-01-14','E026','Max OTP retry exceeded',NULL,NULL,'MAX_OTP_RETRY',NULL,NULL),
(51,'OTP','2025-01-14','E027','Verification OTP expired',NULL,NULL,'VERIFICATION_OTP_EXPIRED',NULL,NULL),
(52,'OTP','2025-01-15','E028','OTP is currently blocked or not found',NULL,NULL,'OTP_BLOCKED_OR_NOT_FOUND',NULL,NULL),
(53,'COMMON','2025-03-20','E029','User existed',NULL,NULL,'USER_EXISTED',NULL,NULL),
(54,'REGISTER','2025-05-10','E000','Users details pass the verification',NULL,NULL,'USER_DETAILS_VERIFIED',NULL,NULL),
(55,'TOKEN','2025-07-01','E030','Token refresh invalid',NULL,NULL,'REFRESH_TOKEN_INVALID',NULL,NULL),
(56,'TOKEN','2025-07-01','E031','Token refresh expired',NULL,NULL,'REFRESH_TOKEN_EXPIRED',NULL,NULL),
(57,'FORGOT_PASSWORD','2025-07-18','E000','New password updated successfully',NULL,NULL,'PASSWORD_UPDATED_SUCCESS',NULL,NULL),
(58,'COMMON','2025-07-29','E000','Search info successfully',NULL,NULL,'SEARCH_INFO_SUCCESS',NULL,NULL),
(59,'TRIP','2026-05-12','E000','Trip created successfully',NULL,NULL,'TRIP_CREATED_SUCCESS',NULL,NULL),
(60,'TRIP','2026-05-16','E000','Trips retrieved successfully',NULL,NULL,'TRIPS_RETRIEVED_SUCCESS',NULL,NULL),
(61,'TRIP','2026-05-16','E032','Trip not found!',NULL,NULL,'TRIP_NOT_FOUND',NULL,NULL),
(62,'TRIP','2026-05-17','E000','Trip updated successfully',NULL,NULL,'TRIP_UPDATED_SUCCESS',NULL,NULL),
(63,'TRIP','2026-05-17','E000','Trip deleted successfully',NULL,NULL,'TRIP_DELETED_SUCCESS',NULL,NULL),
(64,'ACTIVITY','2026-05-23','E000','Activity created successfully',NULL,NULL,'ACTIVITY_CREATED_SUCCESS',NULL,NULL),
(65,'ACTIVITY','2026-05-23','E000','Activity retrieved successfully',NULL,NULL,'ACTIVITY_RETRIEVED_SUCCESS',NULL,NULL),
(66,'ACTIVITY','2026-05-23','E000','Activity updated successfully',NULL,NULL,'ACTIVITY_UPDATED_SUCCESS',NULL,NULL),
(67,'ACTIVITY','2026-05-23','E000','Activity deleted successfully',NULL,NULL,'ACTIVITY_DELETED_SUCCESS',NULL,NULL),
(68,'ACTIVITY','2026-05-23','E033','Activity not found',NULL,NULL,'ACTIVITY_NOT_FOUND',NULL,NULL),
(69,'LOGOUT','2026-05-25','E000','Log out succesully',NULL,NULL,'LOGOUT_SUCCESS',NULL,NULL),
(70,'REGISTER','2026-05-25','E002','Username Username has already been taken',NULL,NULL,'USERNAME_TAKEN',NULL,NULL);
/*!40000 ALTER TABLE `error_codes` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `otp_check`
--

DROP TABLE IF EXISTS `otp_check`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_check` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `is_block` tinyint(1) DEFAULT NULL,
  `newest_otp` varchar(255) DEFAULT NULL,
  `phone_num` varchar(255) DEFAULT NULL,
  `retry_count` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `otp_expiration_time` datetime(6) DEFAULT NULL,
  `otp_restricted_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6210p514kli7mfpa3u8oqmheu` (`username`),
  UNIQUE KEY `UKde8h0pgpxfkt8or7bkdmu3omr` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otp_check`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `otp_check` WRITE;
/*!40000 ALTER TABLE `otp_check` DISABLE KEYS */;
/*!40000 ALTER TABLE `otp_check` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `refresh_token`
--

DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `token_id` binary(16) NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `expired_date` datetime(6) DEFAULT NULL,
  `is_revoked` tinyint(1) DEFAULT 0,
  `session_id` varchar(255) NOT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `revoked_date` datetime(6) DEFAULT NULL,
  `replaced_by_token_id` binary(16) DEFAULT NULL,
  `reuse_detected` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `uk_refresh_token_hash` (`token_hash`),
  KEY `idx_refresh_username` (`username`),
  KEY `idx_refresh_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_token`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `refresh_token` WRITE;
/*!40000 ALTER TABLE `refresh_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `refresh_token` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `restaurants`
--

DROP TABLE IF EXISTS `restaurants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurants` (
  `restaurant_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `restaurant_name` varchar(255) NOT NULL,
  PRIMARY KEY (`restaurant_id`),
  UNIQUE KEY `UKay2atugfnrkejhy33tas5bbq0` (`restaurant_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurants`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `restaurants` WRITE;
/*!40000 ALTER TABLE `restaurants` DISABLE KEYS */;
/*!40000 ALTER TABLE `restaurants` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `session_token`
--

DROP TABLE IF EXISTS `session_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `session_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `token` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7f0dk8axrsvht74e7jgotmhi1` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session_token`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `session_token` WRITE;
/*!40000 ALTER TABLE `session_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `session_token` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `session_token_store`
--

DROP TABLE IF EXISTS `session_token_store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `session_token_store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `token` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbfloy16q4muktt8ga9w4n7yt4` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session_token_store`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `session_token_store` WRITE;
/*!40000 ALTER TABLE `session_token_store` DISABLE KEYS */;
/*!40000 ALTER TABLE `session_token_store` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `sms_contents`
--

DROP TABLE IF EXISTS `sms_contents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_contents` (
  `sms_id` int(11) NOT NULL AUTO_INCREMENT,
  `sms_code` varchar(255) NOT NULL,
  `sms_content` varchar(255) NOT NULL,
  `sms_flow` varchar(255) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `sms_enum` varchar(100) NOT NULL,
  PRIMARY KEY (`sms_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_contents`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `sms_contents` WRITE;
/*!40000 ALTER TABLE `sms_contents` DISABLE KEYS */;
INSERT INTO `sms_contents` VALUES
(1,'SMS01','Your verification code is {otp}','OTP','2024-12-07 00:00:00.000000',NULL,'SMS_OTP_REGISTER');
/*!40000 ALTER TABLE `sms_contents` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `trips`
--

DROP TABLE IF EXISTS `trips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `trips` (
  `trip_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) NOT NULL,
  `trip_name` varchar(255) NOT NULL,
  `destination` varchar(255) NOT NULL,
  `end_date` datetime(6) NOT NULL,
  `start_date` datetime(6) NOT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`trip_id`),
  UNIQUE KEY `UKl3m1atq6ynqyg65m003dc4drc` (`trip_name`),
  KEY `FK8wb14dx6ed0bpp3planbay88u` (`user_id`),
  CONSTRAINT `FK8wb14dx6ed0bpp3planbay88u` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trips`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `trips` WRITE;
/*!40000 ALTER TABLE `trips` DISABLE KEYS */;
/*!40000 ALTER TABLE `trips` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `dob` date NOT NULL,
  `referred_code` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `phone_num` varchar(255) DEFAULT NULL,
  `last_modified_date` date DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL,
  `is_oauth2` tinyint(1) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `Username` (`username`),
  UNIQUE KEY `Email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-05-25 16:17:56
