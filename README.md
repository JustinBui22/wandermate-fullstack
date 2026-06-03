# WanderMate Fullstack

WanderMate is a full-stack mobile travel planning application that allows users to create trips, add destinations inside each trip, and manage activities inside each destination.

The project is designed as a portfolio-ready full-stack app with a React Native Expo frontend, a Spring Boot backend, MariaDB database, JWT authentication, nested CRUD flows, and real itinerary validation rules.

---

## Project Overview

WanderMate helps users organise travel plans using the following structure:

```txt
User
  └── Trip
        └── Destination
              └── Activity
```

Users can manage their full itinerary from high-level trip planning down to individual activities. The app includes validation rules for date ranges, overlap warnings, activity time conflicts, ownership checks, and cascade deletion.

---

## Tech Stack

### Frontend

- React Native
- Expo
- Expo Router
- TypeScript
- Axios
- React Native DateTimePicker

### Backend

- Java
- Spring Boot
- Spring Security
- JWT authentication
- Spring Data JPA / Hibernate
- Maven
- Docker / Docker Compose
- Swagger / OpenAPI

### Database

- MariaDB
- Relational database design
- Foreign key relationships
- Cascade delete for nested itinerary data

---

## Core Features

### Authentication

- User registration
- User login
- JWT-based protected routes
- User ownership checks for trip, destination, and activity data

### Trip Management

- Create trip
- View trip list
- View trip detail
- Edit trip
- Delete trip
- Prevent duplicate trip names for the same user
- Warn when a trip overlaps another existing trip
- Prevent updating a trip so that existing destinations fall outside the trip date range

### Destination Management

- Create destination inside a trip
- View destination list inside trip detail
- View destination detail
- Edit destination
- Delete destination
- Warn when a destination overlaps another destination inside the same trip
- Prevent destination dates from being outside the trip date range
- Delete destination also deletes all related activities

### Activity Management

- Create activity inside a destination
- View activity list inside destination detail
- View activity detail
- Edit activity
- Delete activity
- Prevent activity time from being outside the destination date range
- Prevent activity time overlap with another activity in the same trip

---

## Business Rules

### Trip Rules

| Rule | Behaviour |
|---|---|
| A user cannot have two trips with the same name | Hard block |
| A trip can overlap another trip | Soft warning |
| User can continue after trip overlap warning | Allowed with `allowOverlap: true` |
| Trip dates must include all existing destinations | Hard block |
| Deleting a trip deletes its destinations and activities | Cascade delete |

### Destination Rules

| Rule | Behaviour |
|---|---|
| A destination must belong to a trip | Required |
| Destination dates must stay inside the trip date range | Hard block |
| Destinations can overlap inside the same trip | Soft warning |
| User can continue after destination overlap warning | Allowed with `allowOverlap: true` |
| Deleting a destination deletes all activities inside it | Cascade delete |

### Activity Rules

| Rule | Behaviour |
|---|---|
| An activity must belong to a destination | Required |
| Activity time must stay inside the destination date range | Hard block |
| Activities cannot overlap with another activity in the same trip | Hard block |
| Back-to-back activities are allowed | Allowed |

Example of allowed back-to-back activities:

```txt
Existing activity: 10:00 - 12:00
New activity:      12:00 - 13:00
Result: allowed
```

Example of blocked overlapping activities:

```txt
Existing activity: 10:00 - 12:00
New activity:      11:00 - 13:00
Result: blocked
```

---

## Soft Warnings vs Hard Errors

WanderMate separates warnings from hard validation errors.

### Soft Warning

Soft warnings are used when the data may still be valid depending on user intention.

Current soft warnings:

```txt
TRIP_OVERLAP_WARNING
DESTINATION_OVERLAP_WARNING
```

Flow:

```txt
Frontend sends request with allowOverlap = false
  ↓
Backend detects overlap
  ↓
Backend returns warning response
  ↓
Frontend shows confirmation popup
  ↓
User confirms
  ↓
Frontend sends same request again with allowOverlap = true
  ↓
Backend saves the data
```

### Hard Error

Hard errors are used when the data would become invalid.

Examples:

```txt
TRIP_NAME_ALREADY_EXISTS
TRIP_DATE_CONFLICT_WITH_DESTINATION
DESTINATION_DATE_OUTSIDE_TRIP_RANGE
ACTIVITY_OUTSIDE_DESTINATION_RANGE
ACTIVITY_OVERLAP_ERROR
```

For hard errors, the frontend shows an error alert and does not allow the user to continue.

---

## Local Development Ports

| Service | Local URL / Port |
|---|---|
| Spring Boot backend via IntelliJ | `http://localhost:8080` |
| Expo Metro dev server | `http://localhost:8081` |
| Docker backend | `http://localhost:8082` |
| Docker MariaDB from host | `localhost:3307` |
| MariaDB inside Docker network | `db:3306` |

For Android Emulator accessing local backend:

```txt
http://10.0.2.2:8080
```

For Android Emulator accessing Docker backend:

```txt
http://10.0.2.2:8082
```

---

## Project Structure

```txt
wandermate-fullstack/
  backend/
    src/
    docs/
    docker/
    Dockerfile
    docker-compose.yml
    pom.xml
    README.md

  frontend/
    app/
    src/
    assets/
    package.json
    README.md

  README.md
  .gitignore
```

---

## Running the Backend Locally

Backend-specific setup is documented in:

```txt
backend/README.md
```

Basic local backend run:

```bash
cd backend
mvn spring-boot:run
```

Backend runs on:

```txt
http://localhost:8080
```

Swagger may be available at:

```txt
http://localhost:8080/swagger-ui/index.html
```

Depending on the backend context path, the Swagger URL may include the project path.

---

## Running the Backend with Docker

From the backend folder:

```bash
cd backend
docker compose up --build
```

Docker backend runs on:

```txt
http://localhost:8082
```

MariaDB is exposed to the host on:

```txt
localhost:3307
```

Inside Docker, the backend connects to the database using:

```txt
jdbc:mariadb://db:3306/traveling_app
```

Stop Docker services:

```bash
docker compose down
```

---

## Running the Frontend Locally

Frontend-specific setup is documented in:

```txt
frontend/README.md
```

Basic frontend run:

```bash
cd frontend
npm install
npx expo start -c
```

Then press:

```txt
a
```

to open the Android emulator.

Expo Metro uses:

```txt
localhost:8081
```

This is the React Native development server, not the backend API.

---

## API Route Overview

### Authentication Routes

```txt
POST /api/v1/users/register
POST /api/v1/users/login
POST /api/v1/auth/refresh
```

### Trip Routes

```txt
GET    /api/v1/trips
GET    /api/v1/trips/{tripId}
POST   /api/v1/trips
PUT    /api/v1/trips/{tripId}
DELETE /api/v1/trips/{tripId}
```

### Destination Routes

```txt
GET    /api/v1/trips/{tripId}/destinations
GET    /api/v1/trips/{tripId}/destinations/{destinationId}
POST   /api/v1/trips/{tripId}/destinations
PUT    /api/v1/trips/{tripId}/destinations/{destinationId}
DELETE /api/v1/trips/{tripId}/destinations/{destinationId}
```

### Activity Routes

```txt
GET    /api/v1/trips/{tripId}/destinations/{destinationId}/activities
GET    /api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}
POST   /api/v1/trips/{tripId}/destinations/{destinationId}/activities
PUT    /api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}
DELETE /api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}
```

---

## Frontend Flow

The mobile app uses nested routing to match the itinerary structure.

```txt
Trip List
  ↓
Trip Detail
  ↓
Destination Detail
  ↓
Activity Detail
```

Main frontend routes:

```txt
/trips/create
/trips/{tripId}
/trips/{tripId}/edit
/trips/{tripId}/destinations/create
/trips/{tripId}/destinations/{destinationId}
/trips/{tripId}/destinations/{destinationId}/edit
/trips/{tripId}/destinations/{destinationId}/activities/create
/trips/{tripId}/destinations/{destinationId}/activities/{activityId}
/trips/{tripId}/destinations/{destinationId}/activities/{activityId}/edit
```

---

## Backend Architecture

The backend follows a layered architecture:

```txt
Controller
  ↓
Service
  ↓
Validator / Mapper
  ↓
Repository
  ↓
Database
```

Main backend responsibilities:

- Validate request data
- Enforce ownership rules
- Enforce business rules
- Map entities to response DTOs
- Return consistent response templates
- Store data in MariaDB

---

## Documentation

More backend documentation is available in:

```txt
backend/docs/
```

Suggested documents:

```txt
backend/docs/API_GUIDE.md
backend/docs/ARCHITECTURE.md
backend/docs/AUTH_FLOW.md
backend/docs/DATABASE_SEED.md
backend/docs/DOCKER_SETUP.md
backend/docs/POSTMAN_GUIDE.md
```

Frontend setup is available in:

```txt
frontend/README.md
```

Backend setup is available in:

```txt
backend/README.md
```

---

## Testing Checklist

### Happy Path

```txt
1. Register/login
2. Create trip
3. Open trip detail
4. Edit trip
5. Create destination
6. Open destination detail
7. Edit destination
8. Create activity
9. Open activity detail
10. Edit activity
11. Delete activity
12. Delete destination
13. Delete trip
```

### Validation Tests

```txt
Duplicate trip name
→ hard error

Create overlapping trip
→ warning popup, continue allowed

Edit trip to exclude existing destination
→ hard error

Create destination outside trip date range
→ hard error

Create overlapping destination
→ warning popup, continue allowed

Create activity outside destination date range
→ hard error

Create overlapping activity
→ hard error

Create back-to-back activity
→ allowed
```

---

## Screenshots

Screenshots will be added later.

Suggested screenshots:

```txt
Trip List
Trip Detail
Create Trip
Destination Detail
Activity Detail
Trip Overlap Warning
Destination Overlap Warning
Activity Validation Error
```

---

## Deployment Notes

For local development, the app uses localhost-style URLs and Android emulator networking.

For production deployment, the mobile app should use a public backend API URL such as:

```txt
https://api.wandermate.com
```

In production:

```txt
Mobile App
  ↓
Public Backend API URL
  ↓
Backend Container / Server
  ↓
Database
```

The database should not be accessed directly by the frontend.

---

## Future Improvements

- Add backend unit tests and integration tests
- Add CI/CD pipeline
- Add production deployment configuration
- Add reusable frontend form components
- Add reusable frontend date/time picker component
- Improve frontend loading states and skeleton UI
- Add map integration
- Add AI-generated trip recommendations
- Add image upload for trips and destinations
- Add shared trip collaboration
- Add push notifications
- Add offline support
- Add dark mode
- Add Flyway or Liquibase for database migrations
- Add better audit logging and monitoring
