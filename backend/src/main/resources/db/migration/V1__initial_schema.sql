SET NAMES utf8mb4;
SET SESSION sql_mode = REPLACE(@@SESSION.sql_mode, 'NO_BACKSLASH_ESCAPES', '');
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;


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
INSERT INTO `error_codes` (`error_id`, `flow`, `created_date`, `error_code`, `error_message`, `error_description`, `error_type`, `error_enum`, `modified_date`) VALUES (1,'COMMON','2024-12-03 00:00:00.000000','E001','Invalid input provided',NULL,'ERROR','INVALID_INPUT',NULL),
                                                                                                                                                                       (2,'REGISTER','2024-12-03 00:00:00.000000','E002','Username has already been taken',NULL,'ERROR','PASSWORD_UPDATED_SUCCESS',NULL),
                                                                                                                                                                       (3,'REGISTER','2024-12-03 00:00:00.000000','E003','Email has already been taken',NULL,'ERROR','EMAIL_TAKEN',NULL),
                                                                                                                                                                       (4,'REGISTER','2024-12-03 00:00:00.000000','E004','Password must be 8-20 characters long, include at least one lowercase letter, one uppercase letter, one digit, one special character, and must not contain whitespace, <, >, /, or \\ characters.',NULL,'ERROR','PASSWORD_NOT_QUALIFIED',NULL),
                                                                                                                                                                       (5,'COMMON','2024-12-03 00:00:00.000000','E005','User not found',NULL,'ERROR','USER_NOT_FOUND',NULL),
                                                                                                                                                                       (6,'COMMON','2024-12-03 00:00:00.000000','E006','Client internal server error',NULL,'ERROR','CLIENT_SERVER_ERROR',NULL),
                                                                                                                                                                       (7,'LOGIN','2024-12-03 00:00:00.000000','E007','Password does not match',NULL,'ERROR','PASSWORD_NOT_CORRECT',NULL),
                                                                                                                                                                       (8,'COMMON','2024-12-03 00:00:00.000000','E008','Undefined error code',NULL,'ERROR','UNDEFINED_ERROR_CODE',NULL),
                                                                                                                                                                       (9,'COMMON','2024-12-03 00:00:00.000000','E009','Undefined http status code',NULL,'ERROR','UNDEFINED_HTTP_CODE',NULL),
                                                                                                                                                                       (10,'REGISTER','2024-12-03 00:00:00.000000','E000','User has been created succesfully',NULL,'ERROR','USER_CREATED',NULL),
                                                                                                                                                                       (11,'REGISTER','2024-12-04 00:00:00.000000','E010','Email format is invalid',NULL,'ERROR','EMAIL_PATTERN_INVALID',NULL),
                                                                                                                                                                       (12,'REGISTER','2024-12-05 00:00:00.000000','E011','Phone number must start with 0, 84, or +84, followed by 3, 5, 7, 8, or 9, and contain 7 to 8 digits',NULL,'ERROR','PHONE_FORMAT_INVALID',NULL),
                                                                                                                                                                       (13,'SMS','2024-12-08 00:00:00.000000','E012','SMS config is not found',NULL,'ERROR','SMS_NOT_CONFIG',NULL),
                                                                                                                                                                       (14,'REGISTER','2024-12-08 00:00:00.000000','E013','Username format invalid','Username must have 5-20 characters, no backspace, no tab, and do not match phone pattern ','ERROR','USERNAME_FORMAT_INVALID',NULL),
                                                                                                                                                                       (28,'LOGIN','2024-12-08 00:00:00.000000','E000','User login succesfully',NULL,'ERROR','LOGIN_SUCCESS',NULL),
                                                                                                                                                                       (29,'TOKEN','2024-12-12 00:00:00.000000','E000','Token generate successfully',NULL,'ERROR','TOKEN_GENERATE_SUCCESS',NULL),
                                                                                                                                                                       (30,'TOKEN','2024-12-12 00:00:00.000000','E014','Token generate fail',NULL,'ERROR','TOKEN_GENERATE_FAIL',NULL),
                                                                                                                                                                       (31,'TOKEN','2024-12-12 00:00:00.000000','E015','Token verify fail',NULL,'ERROR','TOKEN_VERIFY_FAIL',NULL),
                                                                                                                                                                       (32,'TOKEN','2024-12-12 00:00:00.000000','E000','Token verified successfully',NULL,'ERROR','TOKEN_VERIFY_SUCCESS',NULL),
                                                                                                                                                                       (33,'TOKEN','2024-12-12 00:00:00.000000','E016','Session expires',NULL,'ERROR','TOKEN_EXPIRE',NULL),
                                                                                                                                                                       (34,'COMMON','2024-12-17 00:00:00.000000','E017','Internal server error',NULL,'ERROR','INTERNAL_SERVER_ERROR',NULL),
                                                                                                                                                                       (35,'COMMON','2024-12-17 00:00:00.000000','E018','Config not found',NULL,'ERROR','CONFIG_NOT_FOUND',NULL),
                                                                                                                                                                       (36,'COMMON','2024-12-21 00:00:00.000000','E019','Input format invalid',NULL,'ERROR','INPUT_FORMAT_INVALID',NULL),
                                                                                                                                                                       (37,'OTP','2024-12-21 00:00:00.000000','E020','OTP code verification fail',NULL,'ERROR','OTP_VERIFICATION_FAIL',NULL),
                                                                                                                                                                       (38,'TOKEN','2025-01-03 00:00:00.000000','E021','Token not found',NULL,'ERROR','TOKEN_NOT_FOUND',NULL),
                                                                                                                                                                       (39,'TOKEN','2025-01-03 00:00:00.000000','E000','Token retrieved successfully',NULL,'ERROR','TOKEN_RETRIEVE_SUCCESS',NULL),
                                                                                                                                                                       (40,'LOGIN','2025-01-06 00:00:00.000000','E022','Max session reached',NULL,'ERROR','MAX_SESSIONS_REACHED',NULL),
                                                                                                                                                                       (42,'TOKEN','2025-01-07 00:00:00.000000','E023','Session token invalid',NULL,'ERROR','SESSION_TOKEN_INVALID',NULL),
                                                                                                                                                                       (43,'OTP','2025-01-08 00:00:00.000000','E000','OTP code verification successfully',NULL,'ERROR','OTP_VERIFICATION_SUCCESS',NULL),
                                                                                                                                                                       (44,'OTP','2025-01-08 00:00:00.000000','E000','Otp created successfully',NULL,'ERROR','OTP_CREATED_SUCCESS',NULL),
                                                                                                                                                                       (45,'OTP','2025-01-08 00:00:00.000000','E000','Otp sent successfully',NULL,'ERROR','OTP_SENT_SUCCESS',NULL),
                                                                                                                                                                       (46,'SMS','2025-01-08 00:00:00.000000','E000','Sms sent successfully',NULL,'ERROR','SMS_SENT_SUCCESS',NULL),
                                                                                                                                                                       (47,'SMS','2025-01-08 00:00:00.000000','E024','Sms sent failed',NULL,'ERROR','SMS_SENT_FAIL',NULL),
                                                                                                                                                                       (48,'EMAIL','2025-01-10 00:00:00.000000','E000','Email sent successfully',NULL,'ERROR','EMAIL_SENT_SUCCESS',NULL),
                                                                                                                                                                       (49,'EMAIL','2025-01-10 00:00:00.000000','E025','Email sent failed',NULL,'ERROR','EMAIL_SENT_FAIL',NULL),
                                                                                                                                                                       (50,'OTP','2025-01-14 00:00:00.000000','E026','Max OTP retry exceeded',NULL,'ERROR','MAX_OTP_RETRY',NULL),
                                                                                                                                                                       (51,'OTP','2025-01-14 00:00:00.000000','E027','Verification OTP expired',NULL,'ERROR','VERIFICATION_OTP_EXPIRED',NULL),
                                                                                                                                                                       (52,'OTP','2025-01-15 00:00:00.000000','E028','OTP is currently blocked or not found',NULL,'ERROR','OTP_BLOCKED_OR_NOT_FOUND',NULL),
                                                                                                                                                                       (53,'COMMON','2025-03-20 00:00:00.000000','E029','User existed',NULL,'ERROR','USER_EXISTED',NULL),
                                                                                                                                                                       (54,'REGISTER','2025-05-10 00:00:00.000000','E000','Users details pass the verification',NULL,'ERROR','USER_DETAILS_VERIFIED',NULL),
                                                                                                                                                                       (55,'TOKEN','2025-07-01 00:00:00.000000','E030','Token refresh invalid',NULL,'ERROR','REFRESH_TOKEN_INVALID',NULL),
                                                                                                                                                                       (56,'TOKEN','2025-07-01 00:00:00.000000','E031','Token refresh expired',NULL,'ERROR','REFRESH_TOKEN_EXPIRED',NULL),
                                                                                                                                                                       (57,'FORGOT_PASSWORD','2025-07-18 00:00:00.000000','E000','New password updated successfully',NULL,'ERROR','PASSWORD_UPDATED_SUCCESS',NULL),
                                                                                                                                                                       (58,'COMMON','2025-07-29 00:00:00.000000','E000','Search info successfully',NULL,'ERROR','SEARCH_INFO_SUCCESS',NULL),
                                                                                                                                                                       (59,'TRIP','2026-05-12 00:00:00.000000','E000','Trip created successfully',NULL,'ERROR','TRIP_CREATED_SUCCESS',NULL),
                                                                                                                                                                       (60,'TRIP','2026-05-16 00:00:00.000000','E000','Trips retrieved successfully',NULL,'ERROR','TRIPS_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (61,'TRIP','2026-05-16 00:00:00.000000','E032','Trip not found!',NULL,'ERROR','TRIP_NOT_FOUND',NULL),
                                                                                                                                                                       (62,'TRIP','2026-05-17 00:00:00.000000','E000','Trip updated successfully',NULL,'ERROR','TRIP_UPDATED_SUCCESS',NULL),
                                                                                                                                                                       (63,'TRIP','2026-05-17 00:00:00.000000','E000','Trip deleted successfully',NULL,'ERROR','TRIP_DELETED_SUCCESS',NULL),
                                                                                                                                                                       (64,'ACTIVITY','2026-05-23 00:00:00.000000','E000','Activity created successfully',NULL,'ERROR','ACTIVITY_CREATED_SUCCESS',NULL),
                                                                                                                                                                       (65,'ACTIVITY','2026-05-23 00:00:00.000000','E000','Activity retrieved successfully',NULL,'ERROR','ACTIVITY_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (66,'ACTIVITY','2026-05-23 00:00:00.000000','E000','Activity updated successfully',NULL,'ERROR','ACTIVITY_UPDATED_SUCCESS',NULL),
                                                                                                                                                                       (67,'ACTIVITY','2026-05-23 00:00:00.000000','E000','Activity deleted successfully',NULL,'ERROR','ACTIVITY_DELETED_SUCCESS',NULL),
                                                                                                                                                                       (68,'ACTIVITY','2026-05-23 00:00:00.000000','E033','Activity not found',NULL,'ERROR','ACTIVITY_NOT_FOUND',NULL),
                                                                                                                                                                       (69,'LOGOUT','2026-05-25 00:00:00.000000','E000','Log out succesully',NULL,'ERROR','LOGOUT_SUCCESS',NULL),
                                                                                                                                                                       (70,'REGISTER','2026-05-25 00:00:00.000000','E002','Username Username has already been taken',NULL,'ERROR','USERNAME_TAKEN',NULL),
                                                                                                                                                                       (71,'DESTINATION','2026-05-31 00:00:00.000000','E000','Destination created successfully','Destination created successfully','ERROR','DESTINATION_CREATED_SUCCESS',NULL),
                                                                                                                                                                       (72,'DESTINATION','2026-05-31 00:00:00.000000','E000','Destination retrieved successfully','Destination retrieved successfully','ERROR','DESTINATION_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (73,'DESTINATION','2026-05-31 00:00:00.000000','E000','Destination updated successfully','Destination updated successfully','ERROR','DESTINATION_UPDATED_SUCCESS',NULL),
                                                                                                                                                                       (74,'DESTINATION','2026-05-31 00:00:00.000000','E000','Destination deleted successfully','Destination deleted successfully','ERROR','DESTINATION_DELETED_SUCCESS',NULL),
                                                                                                                                                                       (75,'DESTINATION','2026-05-31 00:00:00.000000','E034','Destination not found','Destination not found','ERROR','DESTINATION_NOT_FOUND',NULL),
                                                                                                                                                                       (76,'DESTINATION','2026-05-31 00:00:00.000000','W002','This destination overlaps with another destination in this trip','This destination overlaps with another destination in this trip','WARNING','DESTINATION_OVERLAP_WARNING',NULL),
                                                                                                                                                                       (77,'TRIP','2026-05-31 00:00:00.000000','W001','This trip overlaps with another existing trip','This trip overlaps with another existing trip','WARNING','TRIP_OVERLAP_WARNING',NULL),
                                                                                                                                                                       (78,'TRIP','2026-06-01 00:00:00.000000','E035','Trip dates must include all existing destinations','Trip dates must include all existing destinations','ERROR','TRIP_DATE_CONFLICT_WITH_DESTINATION',NULL),
                                                                                                                                                                       (80,'DESTINATION','2026-06-08 00:00:00.000000','E036','Destination dates must stay inside the trip date range','A destination cannot start before the trip starts or end after the trip ends.','ERROR','DESTINATION_DATE_OUTSIDE_TRIP_RANGE',NULL),
                                                                                                                                                                       (81,'ACTIVITY','2026-06-08 00:00:00.000000','E037','Activity time must stay inside the destination date range','An activity cannot start before the destination starts or end after the destination ends.','ERROR','ACTIVITY_OUTSIDE_DESTINATION_RANGE',NULL),
                                                                                                                                                                       (82,'ACTIVITY','2026-06-08 00:00:00.000000','E038','Activity time overlaps with another activity in this trip','The selected activity time conflicts with an existing activity. Activities cannot overlap.','ERROR','ACTIVITY_OVERLAP_ERROR',NULL),
                                                                                                                                                                       (83,'TRIP','2026-06-08 00:00:00.000000','E039','Trip name already exists for this user','A user cannot have two trips with the same trip name.','ERROR','TRIP_NAME_ALREADY_EXISTS',NULL),
                                                                                                                                                                       (84,'COMMON','2026-06-08 00:00:00.000000','E040','Invalid config value','Invalid config value','ERROR','INVALID_CONFIG',NULL),
                                                                                                                                                                       (85,'REGISTER','2026-06-10 00:00:00.000000','E041','Phone number taken','Phone number taken','ERROR','PHONE_NUMBER_TAKEN',NULL),
                                                                                                                                                                       (86,'OTP','2026-06-10 00:00:00.000000','E043','The phone number does not match this account. Please enter the registered phone number.','OTP phone number does not match the provided phone number','ERROR','OTP_PHONE_NOT_MATCH',NULL),
                                                                                                                                                                       (87,'OTP','2026-06-10 00:00:00.000000','E042','The email does not match this account. Please enter the registered email.','OTP email does not match the provided email','ERROR','OTP_EMAIL_NOT_MATCH',NULL),
                                                                                                                                                                       (88,'OTP','2026-06-10 00:00:00.000000','E044','The OTP code is not correct. Please try again.','OTP code is not correct','ERROR','OTP_CODE_NOT_CORRECT',NULL),
                                                                                                                                                                       (89,'REGISTER','2026-06-10 00:00:00.000000','E045','Date of birth cannot be in the future','Date of birth cannot be in the future','ERROR','DOB_IN_FUTURE',NULL),
                                                                                                                                                                       (90,'ACTIVITY','2026-06-11 00:00:00.000000','E046','Activity start time must be before end time','Activity start time must be before end time','BAD_REQUEST','ACTIVITY_TIME_INVALID',NULL),
                                                                                                                                                                       (91,'TRIP','2026-06-11 00:00:00.000000','E047','Trip start date cannot be in the past','Trip start date cannot be in the past','BAD_REQUEST','TRIP_DATE_IN_PAST',NULL),
                                                                                                                                                                       (92,'DESTINATION','2026-06-11 00:00:00.000000','E048','Destination start date cannot be in the past','Destination start date cannot be in the past','BAD_REQUEST','DESTINATION_DATE_IN_PAST',NULL),
                                                                                                                                                                       (93,'TRIP','2026-06-11 00:00:00.000000','E049','Trip date range must include all existing destinations','Trip date range must include all existing destinations','CONFLICT','TRIP_DATE_CONFLICT_WITH_EXISTING_DESTINATION',NULL),
                                                                                                                                                                       (94,'DESTINATION','2026-06-11 00:00:00.000000','E050','Destination date range must include all existing activities in this destination','Destination date range must include all existing activities in this destination','CONFLICT','DESTINATION_DATE_CONFLICT_WITH_EXISTING_ACTIVITY',NULL),
                                                                                                                                                                       (95,'ACTIVITY','2026-06-11 00:00:00.000000','E051','Activity time must not overlap with existing activities in this trip','Activity time must not overlap with existing activities in this trip','CONFLICT','ACTIVITY_TIME_CONFLICT_WITH_EXISTING_ACTIVITY',NULL),
                                                                                                                                                                       (96,'ACTIVITY','2026-06-11 00:00:00.000000','E052','Activity time not found','Activity time not found','BAD_REQUEST','ACTIVITY_TIME_NOT_FOUND',NULL),
                                                                                                                                                                       (97,'ACTIVITY','2026-06-11 00:00:00.000000','E053','Activity name is not found','Activity name is not found','BAD_REQUEST','ACTIVITY_NAME_NOT_FOUND',NULL),
                                                                                                                                                                       (98,'DESTINATION','2026-06-11 00:00:00.000000','E054','Destination name is not found','Destination name is not found','BAD_REQUEST','DESTINATION_NAME_NOT_FOUND',NULL),
                                                                                                                                                                       (99,'DESTINATION','2026-06-11 00:00:00.000000','E055','Destination time not found','Destination time not found','BAD_REQUEST','DESTINATION_TIME_NOT_FOUND',NULL),
                                                                                                                                                                       (100,'DESTINATION','2026-06-11 00:00:00.000000','E056','Destination start time must be before end time','Destination start time must be before end time','BAD_REQUEST','DESTINATION_TIME_INVALID',NULL),
                                                                                                                                                                       (101,'TRIP','2026-06-11 00:00:00.000000','E057','Trip name is not found','Trip name is not found','BAD_REQUEST','TRIP_NAME_NOT_FOUND',NULL),
                                                                                                                                                                       (102,'TRIP','2026-06-11 00:00:00.000000','E058','Trip time not found','Trip time not found','BAD_REQUEST','TRIP_TIME_NOT_FOUND',NULL),
                                                                                                                                                                       (103,'TRIP','2026-06-11 00:00:00.000000','E059','Trip start time must be before end time','Trip start time must be before end time','BAD_REQUEST','TRIP_TIME_INVALID',NULL),
                                                                                                                                                                       (104,'OTP','2026-06-11 00:00:00.000000','E060','OTP verification method is missing','OTP verification method is missing','BAD_REQUEST','OTP_METHOD_MISSING',NULL),
                                                                                                                                                                       (105,'OTP','2026-06-11 00:00:00.000000','E061','Email enum is missing','Email enum is missing','BAD_REQUEST','EMAIL_ENUM_MISSING',NULL),
                                                                                                                                                                       (106,'OTP','2026-06-11 00:00:00.000000','E062','SMS enum is missing','SMS enum is missing','BAD_REQUEST','SMS_ENUM_MISSING',NULL),
                                                                                                                                                                       (107,'FORGOT_PASSWORD','2026-06-14 16:35:16.000000','E063','New password cannot be the same as the old password','New password cannot be the same as the old password','BAD_REQUEST','NEW_PASSWORD_SAME_AS_OLD',NULL),
                                                                                                                                                                       (108,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip invitation sent successfully',NULL,NULL,'TRIP_INVITATION_SENT_SUCCESS',NULL),
                                                                                                                                                                       (109,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip invitations retrieved successfully',NULL,NULL,'TRIP_INVITATIONS_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (110,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip invitation accepted successfully',NULL,NULL,'TRIP_INVITATION_ACCEPTED_SUCCESS',NULL),
                                                                                                                                                                       (111,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip invitation rejected successfully',NULL,NULL,'TRIP_INVITATION_REJECTED_SUCCESS',NULL),
                                                                                                                                                                       (112,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip join request sent successfully',NULL,NULL,'TRIP_JOIN_REQUEST_SENT_SUCCESS',NULL),
                                                                                                                                                                       (113,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip join requests retrieved successfully',NULL,NULL,'TRIP_JOIN_REQUESTS_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (114,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip join request accepted successfully',NULL,NULL,'TRIP_JOIN_REQUEST_ACCEPTED_SUCCESS',NULL),
                                                                                                                                                                       (115,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip join request rejected successfully',NULL,NULL,'TRIP_JOIN_REQUEST_REJECTED_SUCCESS',NULL),
                                                                                                                                                                       (116,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip overlap warnings retrieved successfully',NULL,NULL,'TRIP_OVERLAP_WARNINGS_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (117,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E073','Trip collaboration request not found',NULL,NULL,'TRIP_COLLABORATION_REQUEST_NOT_FOUND',NULL),
                                                                                                                                                                       (118,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E074','A pending collaboration request already exists for this trip and user',NULL,NULL,'TRIP_COLLABORATION_REQUEST_ALREADY_EXISTS',NULL),
                                                                                                                                                                       (119,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E075','Trip owner cannot invite themselves',NULL,NULL,'TRIP_CANNOT_INVITE_SELF',NULL),
                                                                                                                                                                       (120,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E076','Trip owner cannot request to join their own trip',NULL,NULL,'TRIP_OWNER_CANNOT_REQUEST_TO_JOIN_OWN_TRIP',NULL),
                                                                                                                                                                       (121,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip owner cannot request to Trip members retrieved successfully their own trip','Trip members retrieved successfully',NULL,'TRIP_MEMBERS_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (122,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip member role updated successfully','Trip member role updated successfully',NULL,'TRIP_MEMBER_ROLE_UPDATED_SUCCESS',NULL),
                                                                                                                                                                       (123,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip member added successfully','Trip member added successfully',NULL,'TRIP_MEMBER_ADDED_SUCCESS',NULL),
                                                                                                                                                                       (124,'TRIP_MEMBER','2026-06-16 16:49:27.000000','E000','Trip member removed successfully','Trip member removed successfully',NULL,'TRIP_MEMBER_REMOVED_SUCCESS',NULL),
                                                                                                                                                                       (909,'SUGGESTION','2026-06-20 07:21:39.000000','E000','Suggestion created successfully',NULL,NULL,'SUGGESTION_CREATED_SUCCESS',NULL),
                                                                                                                                                                       (910,'SUGGESTION','2026-06-20 07:21:40.000000','E000','Suggestion retrieved successfully',NULL,NULL,'SUGGESTION_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (911,'SUGGESTION','2026-06-20 07:21:41.000000','E000','Suggestion approved successfully',NULL,NULL,'SUGGESTION_APPROVED_SUCCESS',NULL),
                                                                                                                                                                       (912,'SUGGESTION','2026-06-20 07:21:42.000000','E000','Suggestion rejected successfully',NULL,NULL,'SUGGESTION_REJECTED_SUCCESS',NULL),
                                                                                                                                                                       (913,'TRIP_MEMBER','2026-06-20 07:26:02.000000','E064','You do not have permission to access this trip',NULL,NULL,'TRIP_ACCESS_DENIED',NULL),
                                                                                                                                                                       (914,'TRIP_MEMBER','2026-06-20 07:26:02.000000','E065','This user is already a member of the trip',NULL,NULL,'TRIP_MEMBER_ALREADY_EXISTS',NULL),
                                                                                                                                                                       (915,'TRIP_MEMBER','2026-06-20 07:26:02.000000','E066','Trip member not found',NULL,NULL,'TRIP_MEMBER_NOT_FOUND',NULL),
                                                                                                                                                                       (916,'TRIP_MEMBER','2026-06-20 07:26:02.000000','E067','Trip owner cannot be removed from the trip',NULL,NULL,'TRIP_OWNER_CANNOT_BE_REMOVED',NULL),
                                                                                                                                                                       (917,'TRIP_MEMBER','2026-06-20 07:26:02.000000','E068','Trip owner role cannot be changed',NULL,NULL,'TRIP_OWNER_ROLE_CANNOT_BE_CHANGED',NULL),
                                                                                                                                                                       (918,'SUGGESTION','2026-06-20 07:26:02.000000','E069','Suggestion not found',NULL,NULL,'SUGGESTION_NOT_FOUND',NULL),
                                                                                                                                                                       (919,'SUGGESTION','2026-06-20 07:26:02.000000','E070','Suggestion already approved',NULL,NULL,'SUGGESTION_ALREADY_APPROVED',NULL),
                                                                                                                                                                       (920,'TRIP_MEMBER','2026-06-20 07:26:02.000000','E071','Owner role cannot be assigned manually',NULL,NULL,'OWNER_CANNOT_BE_ASSIGNED_MANUALLY',NULL),
                                                                                                                                                                       (921,'SUGGESTION','2026-06-20 07:26:02.000000','E072','Suggestion already rejected',NULL,NULL,'SUGGESTION_ALREADY_REJECTED',NULL),
                                                                                                                                                                       (922,'TRIP','2026-06-21 02:16:42.000000','E077','Trip status is not valid','Trip status is not valid','BAD_REQUEST','TRIP_STATUS_INVALID',NULL),
                                                                                                                                                                       (923,'TRIP_MEMBER','2026-06-21 12:33:57.000000','E000','Trip share code created successfully',NULL,NULL,'TRIP_SHARE_CODE_CREATED_SUCCESS',NULL),
                                                                                                                                                                       (924,'TRIP_MEMBER','2026-06-21 12:34:05.000000','E000','Trip share code retrieved successfully',NULL,NULL,'TRIP_SHARE_CODE_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (925,'TRIP_MEMBER','2026-06-21 12:34:11.000000','E000','Trip share code join request sent successfully',NULL,NULL,'TRIP_SHARE_CODE_JOIN_REQUEST_SENT_SUCCESS',NULL),
                                                                                                                                                                       (926,'TRIP_MEMBER','2026-06-21 12:34:12.000000','E078','Trip share code not found',NULL,NULL,'TRIP_SHARE_CODE_NOT_FOUND',NULL),
                                                                                                                                                                       (927,'TRIP_MEMBER','2026-06-21 12:34:13.000000','E079','Trip share code has expired',NULL,NULL,'TRIP_SHARE_CODE_EXPIRED',NULL),
                                                                                                                                                                       (928,'TRIP_MEMBER','2026-06-21 12:34:14.000000','E080','Trip share code is inactive',NULL,NULL,'TRIP_SHARE_CODE_INACTIVE',NULL),
                                                                                                                                                                       (929,'TRIP_MEMBER','2026-06-21 12:34:15.000000','E081','Trip share code has already been used',NULL,NULL,'TRIP_SHARE_CODE_USED',NULL),
                                                                                                                                                                       (930,'TRIP_MEMBER','2026-06-21 12:34:16.000000','E082','Trip share code has been revoked',NULL,NULL,'TRIP_SHARE_CODE_REVOKED',NULL),
                                                                                                                                                                       (931,'TRIP_MEMBER','2026-06-21 12:34:17.000000','E083','Please wait before generating another trip share code',NULL,NULL,'TRIP_SHARE_CODE_GENERATE_TOO_SOON',NULL),
                                                                                                                                                                       (932,'TRIP_MEMBER','2026-06-21 12:34:19.000000','E084','Too many invalid invite code attempts. Please try again later',NULL,NULL,'TRIP_SHARE_CODE_ATTEMPT_RESTRICTED',NULL),
                                                                                                                                                                       (933,'TRIP_MEMBER','2026-06-27 23:14:44.000000','E000','Collaboration summary retrieved successfully',NULL,NULL,'COLLABORATION_SUMMARY_RETRIEVED_SUCCESS',NULL),
                                                                                                                                                                       (934,'COMMON','2026-07-08 22:27:04.000000','E085','Failed to delete image from cloud storage',NULL,NULL,'DELETE_IMAGE_FAIL',NULL),
                                                                                                                                                                       (935,'OTP','2026-07-18 00:00:00.000000','E086','OTP cooldown period has not expired yet','OTP cooldown period has not expired yet','TOO_MANY_REQUESTS','OTP_COOLDOWN_NOT_EXPIRED',NULL),
                                                                                                                                                                       (936,'REGISTER','2026-07-18 00:00:00.000000','E087','An OTP has already been sent for registration. Please wait before requesting another one.','An OTP has already been sent for registration. Please wait before requesting another one.','TOO_MANY_REQUESTS','REGISTER_OTP_ALREADY_SENT',NULL);


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

CREATE TABLE `accommodations` (
                                  `accommodation_id` int(11) NOT NULL AUTO_INCREMENT,
                                  `accommodation_name` varchar(255) NOT NULL,
                                  `created_date` datetime(6) DEFAULT NULL,
                                  PRIMARY KEY (`accommodation_id`),
                                  UNIQUE KEY `UKpoqt6c2inlbditbx8j9861i8n` (`accommodation_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cities` (
                          `city_id` int(11) NOT NULL AUTO_INCREMENT,
                          `city_name` varchar(255) NOT NULL,
                          `created_date` datetime(6) DEFAULT NULL,
                          PRIMARY KEY (`city_id`),
                          UNIQUE KEY `UKrlmpoah07xxtfr03pmosd593p` (`city_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `restaurants` (
                               `restaurant_id` int(11) NOT NULL AUTO_INCREMENT,
                               `created_date` datetime(6) DEFAULT NULL,
                               `restaurant_name` varchar(255) NOT NULL,
                               PRIMARY KEY (`restaurant_id`),
                               UNIQUE KEY `UKay2atugfnrkejhy33tas5bbq0` (`restaurant_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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



INSERT INTO `configuration` (`config_id`, `created_date`, `config_code`, `config_message`, `config_type`, `modified_date`, `config_value`) VALUES (1,'2024-12-04 00:00:00.000000','PASSWORD_PATTERN','Password must be 8-20 characters long, include at least one lowercase letter, one uppercase letter, one digit, one special character, and must not contain whitespace, <, >, /, or \\ characters.',NULL,NULL,'^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:''",.<>?])[^\\s<>\\\\/]{8,20}$'),
                                                                                                                                                  (2,'2024-12-04 00:00:00.000000','EMAIL_PATTERN','Configuration for email validation pattern',NULL,NULL,'^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
                                                                                                                                                  (3,'2024-12-05 00:00:00.000000','PHONE_VN_PATTERN','Phone numbers must start with either +84, 84, or 0 (03, 05, 07, 08, 09)',NULL,NULL,'^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$'),
                                                                                                                                                  (4,'2024-12-08 00:00:00.000000','USERNAME_PATTERN','Username must have 5-20 characters, no backspace, no tab, and do not match phone pattern ',NULL,NULL,'^(?!^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$)(?!.*[\\u0008\\t])[a-zA-Z0-9_.-]{5,20}$'),
                                                                                                                                                  (5,'2024-12-12 00:00:00.000000','ACCESS_TOKEN_EXPIRATION_TIME','Access-token expiration time in milliseconds',NULL,NULL,'300000'),
                                                                                                                                                  (7,'2024-12-14 00:00:00.000000','NON_AUTHENTICATED_REQUEST','Comma-separated API paths that do not require authentication',NULL,NULL,'/api/v1/users/register,/api/v1/users/login,/api/v1/auth/refresh,/api/v1/users/forgot-password,/api/v1/users/register/verify,/api/v1/otp/send,/api/v1/otp/verify,/api/v1/users/check,/swagger-ui/**,/swagger-ui.html,/v3/api-docs/**,/v3/api-docs.yaml,/v3/api-docs,/api/v1/health'),
                                                                                                                                                  (9,'2025-01-05 00:00:00.000000','MAX_ALLOWED_SESSIONS','Config for the maximum number of allowed sessions.',NULL,NULL,'3'),
                                                                                                                                                  (10,'2025-01-09 00:00:00.000000','OTP_EXPIRATION_TIME','Config for OTP expiration time in ms',NULL,NULL,'300000'),
                                                                                                                                                  (11,'2025-01-10 00:00:00.000000','EMAIL_ADDRESS_CONFIG','Config for email sender',NULL,NULL,'demo@example.com'),
                                                                                                                                                  (12,'2025-01-10 00:00:00.000000','EMAIL_ACCESS_TOKEN_CONFIG','Local placeholder for email OAuth access token',NULL,NULL,'replace_me'),
                                                                                                                                                  (13,'2025-01-10 00:00:00.000000','EMAIL_HOST_CONFIG','Config for email host',NULL,NULL,'smtp.gmail.com'),
                                                                                                                                                  (14,'2025-01-10 00:00:00.000000','EMAIL_PORT_CONFIG','Config for email port',NULL,NULL,'587'),
                                                                                                                                                  (15,'2025-01-11 00:00:00.000000','EMAIL_CLIENT_ID','Local placeholder for Google OAuth client ID',NULL,NULL,'replace_me'),
                                                                                                                                                  (16,'2025-01-11 00:00:00.000000','EMAIL_CLIENT_SECRET','Local placeholder for Google OAuth client secret',NULL,NULL,'replace_me'),
                                                                                                                                                  (17,'2025-01-11 00:00:00.000000','EMAIL_TOKEN_URL','Config for Google OAuth token URL',NULL,NULL,'https://oauth2.googleapis.com/token'),
                                                                                                                                                  (18,'2025-01-11 00:00:00.000000','EMAIL_REFRESH_TOKEN','Config for google refresh token',NULL,NULL,'replace_me'),
                                                                                                                                                  (19,'2025-01-11 00:00:00.000000','EMAIL_REFRESH_ACCESS_TOKEN_RATE','Config for Google OAuth refresh rate in milliseconds',NULL,NULL,'3500000'),
                                                                                                                                                  (20,'2025-01-14 00:00:00.000000','OTP_RESTRICTED_TIME','OTP restriction duration after excessive failed attempts',NULL,NULL,'900000'),
                                                                                                                                                  (21,'2025-01-15 00:00:00.000000','MAX_RETRY_SEND_OTP','Maximum OTP send attempts within a restriction window',NULL,NULL,'3'),
                                                                                                                                                  (22,'2025-07-01 00:00:00.000000','REFRESH_TOKEN_EXPIRATION_TIME','Refresh-token expiration period in months',NULL,NULL,'1'),
                                                                                                                                                  (23,'2026-04-22 21:27:02.000000','MIN_SUGGEST_CHARACTER','Minimum characters required before returning search suggestions',NULL,NULL,'2'),
                                                                                                                                                  (24,'2025-01-15 00:00:00.000000','MAX_RETRY_VERIFY_OTP','Maximum OTP verification attempts within a restriction window',NULL,NULL,'3'),
                                                                                                                                                  (25,'2026-06-10 21:11:58.000000','EMAIL_OAUTH_REFRESH_ENABLED','Whether automatic email OAuth token refresh is enabled',NULL,NULL,'false'),
                                                                                                                                                  (26,'2026-06-28 19:35:02.000000','INVITE_LINK_PREFIX','Config for the prefix of invite link',NULL,NULL,'wandermate://join-trip?code='),
                                                                                                                                                  (27,'2026-07-18 00:00:00.000000','OTP_RETRY_COOLDOWN','Cooldown between OTP requests in milliseconds',NULL,NULL,'60000');
INSERT INTO `email_contents` (`email_id`, `email_code`, `email_content`, `email_flow`, `created_date`, `modified_date`, `email_enum`, `email_subject`) VALUES (1,'EMS01','Hello <b>{name}</b><p>For security purposes, please use the following One-Time Password (OTP) to access your account:</p><h2>{otp}</h2><p>This code was requested from <b>WanderMate</b> and will remain valid for <b>{expire_time}</b> minutes.</p><p>If you did not request this OTP, please ignore this email and do not share the code.</p><p>Safe travels!</p><p><b>The WanderMate Team</b></p>','REGISTER','2025-01-09 00:00:00.000000',NULL,'EMAIL_OTP_REGISTER','Here''s your WanderMate one-time password - expires in {expire_time} minutes');

INSERT INTO `sms_contents` (`sms_id`, `sms_code`, `sms_content`, `sms_flow`, `created_date`, `modified_date`, `sms_enum`) VALUES (1,'SMS01','Your verification code is {otp}','OTP','2024-12-07 00:00:00.000000',NULL,'SMS_OTP_REGISTER');


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
