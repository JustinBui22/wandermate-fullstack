DELETE FROM `otp_check`;

ALTER TABLE `otp_check`
    MODIFY COLUMN `newest_otp` varchar(64) DEFAULT NULL,
    ADD COLUMN `otp_purpose` varchar(32) NOT NULL DEFAULT 'REGISTRATION' AFTER `newest_otp`,
    ADD CONSTRAINT `chk_otp_check_purpose`
    CHECK (`otp_purpose` IN ('REGISTRATION', 'PASSWORD_RESET'));