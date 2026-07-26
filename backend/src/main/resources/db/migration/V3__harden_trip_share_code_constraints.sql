CREATE TEMPORARY TABLE `duplicate_active_trip_share_codes` (
    `share_code_id` bigint(20) NOT NULL,
    PRIMARY KEY (`share_code_id`)
);

INSERT INTO `duplicate_active_trip_share_codes` (`share_code_id`)
SELECT older_share_code.`share_code_id`
FROM `trip_share_codes` older_share_code
JOIN `trip_share_codes` newer_share_code
    ON newer_share_code.`trip_id` = older_share_code.`trip_id`
   AND newer_share_code.`code_status` = 'ACTIVE'
   AND (
        newer_share_code.`created_date` > older_share_code.`created_date`
        OR (
            newer_share_code.`created_date` = older_share_code.`created_date`
            AND newer_share_code.`share_code_id` > older_share_code.`share_code_id`
        )
   )
WHERE older_share_code.`code_status` = 'ACTIVE'
GROUP BY older_share_code.`share_code_id`;

UPDATE `trip_share_codes` share_code
JOIN `duplicate_active_trip_share_codes` duplicate_active_code
    ON duplicate_active_code.`share_code_id` = share_code.`share_code_id`
SET share_code.`code_status` = 'REVOKED',
    share_code.`modified_date` = CURRENT_TIMESTAMP(6);

DROP TEMPORARY TABLE `duplicate_active_trip_share_codes`;

ALTER TABLE `trip_share_codes`
    ADD COLUMN `active_trip_id` bigint(20)
        GENERATED ALWAYS AS (
            CASE
                WHEN `code_status` = 'ACTIVE' THEN `trip_id`
                ELSE NULL
            END
        ) PERSISTENT,
    ADD UNIQUE KEY `uk_trip_share_codes_one_active_per_trip` (`active_trip_id`);
