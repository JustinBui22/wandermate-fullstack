INSERT INTO `error_codes` (
    `flow`,
    `created_date`,
    `error_code`,
    `error_message`,
    `error_description`,
    `error_type`,
    `error_enum`,
    `modified_date`
)
SELECT
    'COMMON',
    CURRENT_TIMESTAMP(6),
    'E088',
    'Too many account verification requests. Please try again later.',
    'Too many account verification requests. Please try again later.',
    'TOO_MANY_REQUESTS',
    'ACCOUNT_ENUMERATION_RATE_LIMITED',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM `error_codes`
    WHERE `error_code` = 'E088'
      AND `flow` = 'COMMON'
      AND `error_enum` = 'ACCOUNT_ENUMERATION_RATE_LIMITED'
);

UPDATE `configuration`
SET `config_value` = REPLACE(
        `config_value`,
        '/api/v1/users/check,',
        ''
    ),
    `modified_date` = CURRENT_TIMESTAMP(6)
WHERE `config_code` = 'NON_AUTHENTICATED_REQUEST'
  AND `config_value` LIKE '%/api/v1/users/check,%';
