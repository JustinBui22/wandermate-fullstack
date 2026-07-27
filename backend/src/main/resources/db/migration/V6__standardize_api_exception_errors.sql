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
    'E089',
    'Request method not allowed',
    'The requested HTTP method is not supported for this endpoint.',
    'METHOD_NOT_ALLOWED',
    'REQUEST_METHOD_NOT_SUPPORTED',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM `error_codes`
    WHERE `error_code` = 'E089'
      AND `flow` = 'COMMON'
      AND `error_enum` = 'REQUEST_METHOD_NOT_SUPPORTED'
);

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
    'E090',
    'Media type not supported',
    'The request content type is not supported for this endpoint.',
    'UNSUPPORTED_MEDIA_TYPE',
    'MEDIA_TYPE_NOT_SUPPORTED',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM `error_codes`
    WHERE `error_code` = 'E090'
      AND `flow` = 'COMMON'
      AND `error_enum` = 'MEDIA_TYPE_NOT_SUPPORTED'
);

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
    'E091',
    'Uploaded file is too large',
    'The uploaded file or multipart request exceeds the configured size limit.',
    'PAYLOAD_TOO_LARGE',
    'PAYLOAD_TOO_LARGE',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM `error_codes`
    WHERE `error_code` = 'E091'
      AND `flow` = 'COMMON'
      AND `error_enum` = 'PAYLOAD_TOO_LARGE'
);

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
    'E092',
    'Resource not found',
    'The requested API resource was not found.',
    'NOT_FOUND',
    'RESOURCE_NOT_FOUND',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM `error_codes`
    WHERE `error_code` = 'E092'
      AND `flow` = 'COMMON'
      AND `error_enum` = 'RESOURCE_NOT_FOUND'
);

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
    'E093',
    'Access denied',
    'You do not have permission to perform this action.',
    'FORBIDDEN',
    'ACCESS_DENIED',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM `error_codes`
    WHERE `error_code` = 'E093'
      AND `flow` = 'COMMON'
      AND `error_enum` = 'ACCESS_DENIED'
);