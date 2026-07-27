ALTER TABLE `trips`
    MODIFY COLUMN `start_date` date NOT NULL,
    MODIFY COLUMN `end_date` date NOT NULL;

ALTER TABLE `trip_destinations`
    MODIFY COLUMN `start_date` date NOT NULL,
    MODIFY COLUMN `end_date` date NOT NULL;