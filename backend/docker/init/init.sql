-- WanderMate local Docker database initialization
-- Generated from the latest cloud database dump, but sanitized for GitHub/local development.
-- This file keeps the current V4 schema and seeds only reference/configuration data.
-- It intentionally does NOT include users, trips, OTPs, refresh/session tokens, share codes, or demo data.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;


DROP TABLE IF EXISTS `configuration`;
CREATE TABLE `configuration` (
                                 `config_id` int(11) NOT NULL AUTO_INCREMENT,
                                 `created_date` datetime(6) DEFAULT NULL,
                                 `config_code` varchar(255) NOT NULL,
                                 `config_message` varchar(255) DEFAULT NULL,
                                 `config_type` varchar(255) DEFAULT NULL,
                                 `modified_date` datetime(6) DEFAULT NULL,
                                 `config_value` varchar(10000) NOT NULL,
                                 PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `error_codes`;
CREATE TABLE `error_codes` (
                               `error_id` int(11) NOT NULL AUTO_INCREMENT,
                               `flow` varchar(255) DEFAULT NULL,
                               `created_date` datetime(6) DEFAULT NULL,
                               `error_code` varchar(255) NOT NULL,
                               `error_message` varchar(255) DEFAULT NULL,
                               `error_description` varchar(255) DEFAULT NULL,
                               `error_type` varchar(255) DEFAULT NULL,
                               `error_enum` varchar(255) DEFAULT NULL,
                               `modified_date` datetime(6) DEFAULT NULL,
                               PRIMARY KEY (`error_id`),
                               UNIQUE KEY `UKsdw4mfc7rh54yl4shlr4ukpi3` (`error_code`,`flow`,`error_enum`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `email_contents`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `sms_contents`;
CREATE TABLE `sms_contents` (
                                `sms_id` int(11) NOT NULL AUTO_INCREMENT,
                                `sms_code` varchar(255) NOT NULL,
                                `sms_content` varchar(255) NOT NULL,
                                `sms_flow` varchar(255) NOT NULL,
                                `created_date` datetime(6) DEFAULT NULL,
                                `modified_date` datetime(6) DEFAULT NULL,
                                `sms_enum` varchar(100) NOT NULL,
                                PRIMARY KEY (`sms_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `accommodations`;
CREATE TABLE `accommodations` (
                                  `accommodation_id` int(11) NOT NULL AUTO_INCREMENT,
                                  `accommodation_name` varchar(255) NOT NULL,
                                  `created_date` datetime(6) DEFAULT NULL,
                                  PRIMARY KEY (`accommodation_id`),
                                  UNIQUE KEY `UKpoqt6c2inlbditbx8j9861i8n` (`accommodation_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities` (
                          `city_id` int(11) NOT NULL AUTO_INCREMENT,
                          `city_name` varchar(255) NOT NULL,
                          `created_date` datetime(6) DEFAULT NULL,
                          PRIMARY KEY (`city_id`),
                          UNIQUE KEY `UKrlmpoah07xxtfr03pmosd593p` (`city_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `restaurants`;
CREATE TABLE `restaurants` (
                               `restaurant_id` int(11) NOT NULL AUTO_INCREMENT,
                               `created_date` datetime(6) DEFAULT NULL,
                               `restaurant_name` varchar(255) NOT NULL,
                               PRIMARY KEY (`restaurant_id`),
                               UNIQUE KEY `UKay2atugfnrkejhy33tas5bbq0` (`restaurant_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `users`;
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
                         `display_name` varchar(255) DEFAULT NULL,
                         `modified_date` datetime(6) DEFAULT NULL,
                         `preferred_theme` enum('DARK','LIGHT','SYSTEM') DEFAULT NULL,
                         `profile_image_url` varchar(500) DEFAULT NULL,
                         `profile_image_public_id` varchar(500) DEFAULT NULL,
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `Username` (`username`),
                         UNIQUE KEY `Email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `trips`;
CREATE TABLE `trips` (
                         `trip_id` bigint(20) NOT NULL AUTO_INCREMENT,
                         `created_date` datetime(6) NOT NULL,
                         `trip_name` varchar(255) NOT NULL,
                         `destination` varchar(255) NOT NULL,
                         `end_date` datetime(6) NOT NULL,
                         `start_date` datetime(6) NOT NULL,
                         `modified_date` datetime(6) DEFAULT NULL,
                         `user_id` bigint(20) NOT NULL,
                         `trip_status` varchar(20) NOT NULL DEFAULT 'PLANNING',
                         `cover_image_url` varchar(500) DEFAULT NULL,
                         `cover_image_public_id` varchar(500) DEFAULT NULL,
                         PRIMARY KEY (`trip_id`),
                         UNIQUE KEY `uk_trips_user_trip_name` (`user_id`,`trip_name`),
                         CONSTRAINT `FK8wb14dx6ed0bpp3planbay88u` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
                         CONSTRAINT `chk_trips_status` CHECK (`trip_status` in ('PLANNING','ONGOING','FINISHED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `trip_destinations`;
CREATE TABLE `trip_destinations` (
                                     `destination_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                     `destination_name` varchar(150) NOT NULL,
                                     `start_date` datetime NOT NULL,
                                     `end_date` datetime NOT NULL,
                                     `destination_order` int(11) DEFAULT NULL,
                                     `notes` varchar(1000) DEFAULT NULL,
                                     `created_date` datetime NOT NULL DEFAULT current_timestamp(),
                                     `modified_date` datetime DEFAULT NULL ON UPDATE current_timestamp(),
                                     `trip_id` bigint(20) NOT NULL,
                                     `created_by_user_id` bigint(20) DEFAULT NULL,
                                     `modified_by_user_id` bigint(20) DEFAULT NULL,
                                     PRIMARY KEY (`destination_id`),
                                     KEY `idx_trip_destinations_trip_id` (`trip_id`),
                                     KEY `idx_trip_destinations_trip_order` (`trip_id`,`destination_order`),
                                     KEY `idx_trip_destinations_created_by_user_id` (`created_by_user_id`),
                                     KEY `idx_trip_destinations_modified_by_user_id` (`modified_by_user_id`),
                                     CONSTRAINT `fk_trip_destinations_created_by_user` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
                                     CONSTRAINT `fk_trip_destinations_modified_by_user` FOREIGN KEY (`modified_by_user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
                                     CONSTRAINT `fk_trip_destinations_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `destination_activities`;
CREATE TABLE `destination_activities` (
                                          `activity_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                          `activity_name` varchar(255) NOT NULL,
                                          `created_date` datetime(6) NOT NULL,
                                          `description` varchar(255) DEFAULT NULL,
                                          `end_date_time` datetime(6) NOT NULL,
                                          `location` varchar(255) DEFAULT NULL,
                                          `modified_date` datetime(6) DEFAULT NULL,
                                          `start_date_time` datetime(6) NOT NULL,
                                          `destination_id` bigint(20) NOT NULL,
                                          `created_by_user_id` bigint(20) NOT NULL,
                                          `modified_by_user_id` bigint(20) DEFAULT NULL,
                                          PRIMARY KEY (`activity_id`),
                                          KEY `idx_activities_start_date_time` (`start_date_time`),
                                          KEY `idx_activities_destination_id` (`destination_id`),
                                          KEY `FKmxmb00mvji4n43oi8d8h7os1q` (`modified_by_user_id`),
                                          KEY `FKpmwnp8lehwvpdge3gvme1wa4g` (`created_by_user_id`),
                                          CONSTRAINT `FKmxmb00mvji4n43oi8d8h7os1q` FOREIGN KEY (`modified_by_user_id`) REFERENCES `users` (`user_id`),
                                          CONSTRAINT `FKpmwnp8lehwvpdge3gvme1wa4g` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`user_id`),
                                          CONSTRAINT `fk_activities_destination` FOREIGN KEY (`destination_id`) REFERENCES `trip_destinations` (`destination_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `trip_members`;
CREATE TABLE `trip_members` (
                                `trip_member_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `created_date` datetime(6) NOT NULL,
                                `modified_date` datetime(6) DEFAULT NULL,
                                `role` enum('ACCEPTED','ACTIVE','ALL','CANCELLED','CREATED','CREATED_DATE_ASC','CREATED_DATE_DESC','EDITOR','EXPIRED','FINISHED','INVITATION','JOINED','JOIN_REQUEST','MODIFIED_DATE_ASC','MODIFIED_DATE_DESC','NAME_ASC','NAME_DESC','ONGOING','OWNER','PENDING','PLANNING','REJECTED','REVOKED','USED','VIEWER') NOT NULL,
                                `trip_id` bigint(20) NOT NULL,
                                `user_id` bigint(20) NOT NULL,
                                PRIMARY KEY (`trip_member_id`),
                                UNIQUE KEY `uk_trip_members_trip_user` (`trip_id`,`user_id`),
                                KEY `FK652lcpk0gdigjfm28bhgu24y2` (`user_id`),
                                CONSTRAINT `FK652lcpk0gdigjfm28bhgu24y2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
                                CONSTRAINT `fk_trip_members_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `trip_collaboration_requests`;
CREATE TABLE `trip_collaboration_requests` (
                                               `request_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                               `created_date` datetime(6) NOT NULL,
                                               `modified_date` datetime(6) DEFAULT NULL,
                                               `request_type` enum('ACCEPTED','ACTIVE','ALL','CANCELLED','CREATED','CREATED_DATE_ASC','CREATED_DATE_DESC','EDITOR','EXPIRED','FINISHED','INVITATION','JOINED','JOIN_REQUEST','MODIFIED_DATE_ASC','MODIFIED_DATE_DESC','NAME_ASC','NAME_DESC','ONGOING','OWNER','PENDING','PLANNING','REJECTED','REVOKED','USED','VIEWER') NOT NULL,
                                               `requested_role` enum('ACCEPTED','ACTIVE','ALL','CANCELLED','CREATED','CREATED_DATE_ASC','CREATED_DATE_DESC','EDITOR','EXPIRED','FINISHED','INVITATION','JOINED','JOIN_REQUEST','MODIFIED_DATE_ASC','MODIFIED_DATE_DESC','NAME_ASC','NAME_DESC','ONGOING','OWNER','PENDING','PLANNING','REJECTED','REVOKED','USED','VIEWER') NOT NULL,
                                               `responded_date` datetime(6) DEFAULT NULL,
                                               `status` enum('ACCEPTED','ACTIVE','ALL','CANCELLED','CREATED','CREATED_DATE_ASC','CREATED_DATE_DESC','EDITOR','EXPIRED','FINISHED','INVITATION','JOINED','JOIN_REQUEST','MODIFIED_DATE_ASC','MODIFIED_DATE_DESC','NAME_ASC','NAME_DESC','ONGOING','OWNER','PENDING','PLANNING','REJECTED','REVOKED','USED','VIEWER') NOT NULL,
                                               `requester_user_id` bigint(20) NOT NULL,
                                               `target_user_id` bigint(20) NOT NULL,
                                               `trip_id` bigint(20) NOT NULL,
                                               PRIMARY KEY (`request_id`),
                                               KEY `FKt4ud1cw8ykh7o92agud52lb97` (`requester_user_id`),
                                               KEY `FK81h149y1w6f1xmrmh4qa47s4p` (`target_user_id`),
                                               KEY `fk_trip_collaboration_requests_trip` (`trip_id`),
                                               CONSTRAINT `FK81h149y1w6f1xmrmh4qa47s4p` FOREIGN KEY (`target_user_id`) REFERENCES `users` (`user_id`),
                                               CONSTRAINT `FKt4ud1cw8ykh7o92agud52lb97` FOREIGN KEY (`requester_user_id`) REFERENCES `users` (`user_id`),
                                               CONSTRAINT `fk_trip_collaboration_requests_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `trip_share_code_attempts`;
CREATE TABLE `trip_share_code_attempts` (
                                            `attempt_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                            `user_id` bigint(20) NOT NULL,
                                            `retry_count` int(11) NOT NULL DEFAULT 0,
                                            `restricted_until` datetime(6) DEFAULT NULL,
                                            `last_attempt_date` datetime(6) DEFAULT NULL,
                                            `created_date` datetime(6) NOT NULL,
                                            `modified_date` datetime(6) DEFAULT NULL,
                                            PRIMARY KEY (`attempt_id`),
                                            UNIQUE KEY `uk_trip_share_code_attempts_user` (`user_id`),
                                            KEY `idx_trip_share_code_attempts_user_id` (`user_id`),
                                            KEY `idx_trip_share_code_attempts_restricted_until` (`restricted_until`),
                                            CONSTRAINT `fk_trip_share_code_attempts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `trip_share_codes`;
CREATE TABLE `trip_share_codes` (
                                    `share_code_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                    `trip_id` bigint(20) NOT NULL,
                                    `code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
                                    `created_by_user_id` bigint(20) NOT NULL,
                                    `default_role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'VIEWER',
                                    `code_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
                                    `expires_at` datetime(6) NOT NULL,
                                    `used_by_user_id` bigint(20) DEFAULT NULL,
                                    `used_date` datetime(6) DEFAULT NULL,
                                    `created_date` datetime(6) NOT NULL,
                                    `modified_date` datetime(6) DEFAULT NULL,
                                    PRIMARY KEY (`share_code_id`),
                                    UNIQUE KEY `uk_trip_share_codes_code` (`code`),
                                    KEY `fk_trip_share_codes_created_by` (`created_by_user_id`),
                                    KEY `fk_trip_share_codes_used_by` (`used_by_user_id`),
                                    KEY `idx_trip_share_codes_trip_id` (`trip_id`),
                                    KEY `idx_trip_share_codes_code` (`code`),
                                    KEY `idx_trip_share_codes_trip_status` (`trip_id`,`code_status`),
                                    KEY `idx_trip_share_codes_created_date` (`created_date`),
                                    CONSTRAINT `fk_trip_share_codes_created_by` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
                                    CONSTRAINT `fk_trip_share_codes_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`) ON DELETE CASCADE,
                                    CONSTRAINT `fk_trip_share_codes_used_by` FOREIGN KEY (`used_by_user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
                                    CONSTRAINT `chk_trip_share_codes_default_role` CHECK (`default_role` in ('EDITOR','VIEWER')),
                                    CONSTRAINT `chk_trip_share_codes_status` CHECK (`code_status` in ('ACTIVE','USED','EXPIRED','REVOKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `otp_check`;
CREATE TABLE `otp_check` (
                             `otp_check_id` int(11) NOT NULL AUTO_INCREMENT,
                             `created_date` datetime(6) DEFAULT NULL,
                             `email` varchar(255) DEFAULT NULL,
                             `is_block` tinyint(1) DEFAULT NULL,
                             `newest_otp` varchar(255) DEFAULT NULL,
                             `phone_num` varchar(255) DEFAULT NULL,
                             `username` varchar(255) NOT NULL,
                             `otp_expiration_time` datetime(6) DEFAULT NULL,
                             `otp_restricted_time` datetime(6) DEFAULT NULL,
                             `retry_send_otp_count` int(11) NOT NULL DEFAULT 0,
                             `retry_verify_otp_count` int(11) NOT NULL DEFAULT 0,
                             PRIMARY KEY (`otp_check_id`),
                             UNIQUE KEY `UK6210p514kli7mfpa3u8oqmheu` (`username`),
                             UNIQUE KEY `UKde8h0pgpxfkt8or7bkdmu3omr` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `refresh_token`;
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

DROP TABLE IF EXISTS `session_token`;
CREATE TABLE `session_token` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `created_date` datetime(6) DEFAULT NULL,
                                 `modified_date` datetime(6) DEFAULT NULL,
                                 `session_id` varchar(255) DEFAULT NULL,
                                 `token` varchar(255) NOT NULL,
                                 `username` varchar(255) NOT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `UK7f0dk8axrsvht74e7jgotmhi1` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------------------------
-- Reference data
-- ---------------------------------------------------------------------------
-- Only safe reference/config rows are inserted below.
-- Runtime tables such as users, trips, tokens, OTPs, collaboration requests,
-- trip members, destinations, and activities are intentionally left empty.



INSERT INTO `configuration` (`config_id`, `created_date`, `config_code`, `config_message`, `config_type`, `modified_date`, `config_value`) VALUES (1,'2024-12-04 00:00:00.000000','PASSWORD_PATTERN','Password must be 8-20 characters long, include at least one lowercase letter, one uppercase letter, one digit, one special character, and must not contain whitespace, <, >, /, or \\ characters.',NULL,NULL,'^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:\'\",.<>?])[^\\s<>\\\\/]{8,20}$');

INSERT INTO `email_contents` (`email_id`, `email_code`, `email_content`, `email_flow`, `created_date`, `modified_date`, `email_enum`, `email_subject`) VALUES (1,'EMS01','Hello <b>{name}</b>,  \n\n<p>For security purposes, please use the following One-Time Password (OTP) to access your account:</p>  \n\n<h2 style=\"color: #2c3e50;\">{otp}</h2>  \n\n<p>This code was requested from the <b>Travelling App</b> and will remain valid for <b>{expire_time}</b> minutes.</p>  \n\n<p>If you did not request this OTP, please ignore this email. Do not share this code with anyone for security reasons.</p>  \n\n<p>Safe travels!</p>  \n\nBest regards,  \n<p><b>The Travelling App Team</b></p>\n','REGISTER','2025-01-09 00:00:00.000000',NULL,'EMAIL_OTP_REGISTER','Here\'s your One Time Password (OTP) - Expire in {expire_time} minutes!');

INSERT INTO `sms_contents` (`sms_id`, `sms_code`, `sms_content`, `sms_flow`, `created_date`, `modified_date`, `sms_enum`) VALUES (1,'SMS01','Your verification code is {otp}','OTP','2024-12-07 00:00:00.000000',NULL,'SMS_OTP_REGISTER');

INSERT INTO `error_codes` (`error_id`, `flow`, `created_date`, `error_code`, `error_message`, `error_description`, `error_type`, `error_enum`, `modified_date`) VALUES (1,'COMMON','2024-12-03 00:00:00.000000','E001','Invalid input provided',NULL,'ERROR','INVALID_INPUT',NULL);

-- Clean up known dirty rows from the source dump.
-- This duplicate/incorrect enum row is not used by the application and conflicts conceptually
-- with the correct USERNAME_TAKEN row.
DELETE FROM `error_codes`
WHERE `error_id` = 2
  AND `flow` = 'REGISTER'
  AND `error_code` = 'E002'
  AND `error_enum` = 'PASSWORD_UPDATED_SUCCESS';

UPDATE `error_codes`
SET `error_message` = 'Trip members retrieved successfully',
    `error_description` = 'Trip members retrieved successfully'
WHERE `error_enum` = 'TRIP_MEMBERS_RETRIEVED_SUCCESS'
  AND `flow` = 'TRIP_MEMBER';

UPDATE `error_codes`
SET `error_message` = 'User created successfully'
WHERE `error_enum` = 'USER_CREATED'
  AND `flow` = 'REGISTER';

UPDATE `error_codes`
SET `error_message` = 'User logged out successfully'
WHERE `error_enum` = 'LOGOUT_SUCCESS'
  AND `flow` = 'LOGOUT';

SET UNIQUE_CHECKS = 1;
SET FOREIGN_KEY_CHECKS = 1;