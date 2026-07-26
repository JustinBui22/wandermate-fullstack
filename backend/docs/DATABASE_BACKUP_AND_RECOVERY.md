# Database Backup and Recovery

## Overview

The WanderMate production database is hosted on a managed MariaDB provider.

The application is responsible for data integrity and schema versioning through Flyway, while the database provider is responsible for backup storage and restoration.

Current production configuration:

- Database: MariaDB
- Hosting: Files.io Managed Database
- Schema management: Flyway
- Hibernate mode: `validate`
- Automatic backups: Enabled
- Manual backups: Supported
- Database restore: Supported

## Objectives

The backup and recovery strategy protects against:

- Infrastructure failure
- Accidental data deletion
- Faulty SQL execution
- Failed deployments
- Failed Flyway migrations
- Data corruption

## Backup Policy

### Automatic backups

- Frequency: Daily
- Retention: 7 days
- Managed by: Database provider

### Manual backups

A manual backup should be created before:

- Production deployment
- Flyway migration
- Database maintenance
- Large data import

Manual backups provide an immediate recovery point if a deployment or migration fails.

## Deployment Checklist

Before every production deployment:

- [ ] Verify all backend tests pass
- [ ] Verify the Flyway migration
- [ ] Create a manual database backup
- [ ] Verify the backup completed successfully
- [ ] Deploy the backend
- [ ] Verify the Flyway migration completed
- [ ] Verify the health endpoint
- [ ] Verify login
- [ ] Verify refresh token
- [ ] Verify Trip CRUD
- [ ] Verify image upload

A deployment is considered complete only after the smoke tests pass.

## Disaster Recovery Procedure

### Scenario 1: Faulty deployment or SQL change

Examples:

- Incorrect Flyway migration
- Incorrect SQL update
- Accidental `DELETE`
- Accidental schema change

Recovery procedure:

1. Stop or roll back the faulty production deployment.
2. Identify the latest healthy backup created before the incident.
3. Restore the backup into a separate database where possible.
4. Redeploy the previous stable backend version.
5. Configure the backend to use the restored database.
6. Verify application health.
7. Perform the recovery validation checklist.
8. Resume normal operation only after validation succeeds.

### Scenario 2: Database corruption or provider failure

1. Restore the latest healthy backup.
2. Verify the database schema and critical table data.
3. Verify the Flyway schema history.
4. Start the backend using the restored database.
5. Perform the recovery validation checklist.
6. Resume normal operation only after validation succeeds.

## Recovery Validation Checklist

### Authentication

- [ ] User login works
- [ ] Access token authentication works
- [ ] Refresh token rotation works
- [ ] Logout and session revocation work

### Trips

- [ ] Trips can be retrieved
- [ ] A trip can be created
- [ ] A trip can be edited
- [ ] A trip can be deleted

### Destinations and activities

- [ ] A destination can be created and updated
- [ ] An activity can be created and updated

### Media

- [ ] Image upload works
- [ ] Image replacement works
- [ ] Image deletion or cleanup works

### Permissions and collaboration

- [ ] Owner permissions work
- [ ] Collaborator permissions work
- [ ] Share-code or invitation flow works

### System

- [ ] Health endpoint responds successfully
- [ ] `flyway_schema_history` exists
- [ ] All Flyway entries report `success = 1`
- [ ] Hibernate schema validation succeeds
- [ ] No unexpected startup errors appear in logs

## Recovery Drill

A database recovery drill has been successfully completed.

Procedure performed:

1. A managed production backup was selected.
2. The backup was restored into a separate database.
3. The backend was configured to use the restored database.
4. The backend started successfully.
5. Flyway and Hibernate schema validation completed successfully.
6. Authentication and core application workflows were tested.

Result:

**PASS**

The application successfully started and operated using the restored database.

## Flyway Integration

Flyway is responsible for database schema versioning.

Production uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=false
```

Hibernate validates the schema but does not create or modify it.

All future schema changes must be introduced through new Flyway migration files, such as:

```text
V2__add_example_column.sql
V3__create_example_table.sql
```

Previously applied migration files must not be edited.

## Rollback Strategy

### Application rollback

Redeploy the previous stable backend build or commit.

### Database recovery

Restore the latest healthy backup created before the faulty database change.

A database restore and an application rollback may both be required when a deployment contains incompatible code and schema changes.

## Recovery Objectives

### Recovery Point Objective

Target maximum data loss:

- Up to 24 hours when relying on the latest automatic daily backup
- Less when a manual pre-deployment backup is available

### Recovery Time Objective

Target restoration and verification time:

- Less than 30 minutes for the current portfolio-scale deployment

These are operational targets rather than provider guarantees.

## Security

Database backups:

- Contain production data
- Must not be committed to Git
- Must not be included in public project archives
- Must only be downloaded to trusted devices
- Must only be restored into trusted environments
- Must be deleted securely when no longer required

Database passwords, connection URLs, tokens, and provider credentials must be stored in environment variables or secret-management settings.

## Production Backup Configuration

The managed MariaDB provider currently supports:

- Scheduled backups
- Daily backup creation
- Backup download
- One-click restore
- Approximately seven days of retention

A manual backup should be created immediately before any production deployment that includes a Flyway migration or other significant database change.

## Operational Summary

```text
Production application
        |
        v
Managed MariaDB database
        |
        v
Automatic daily backups
        |
        +--> Download backup
        |
        +--> Restore backup
                |
                v
        Recovered database
                |
                v
        Backend startup and smoke tests
```

A restored database is not considered ready for production until the recovery validation checklist has passed.
