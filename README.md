# Northline Motors Showroom

A dealership web application built with Spring Boot and MySQL. Customers browse
available vehicles and request test drives; staff manage the inventory and the
bookings. Northline Motors and its vehicle names are fictional.

## Features

**Showroom page** (`http://localhost:8080/`)
- Browse vehicles that are available today.
- Filter by make, body type, and maximum price.
- Book a test drive: name, email, phone, and a preferred date after today.

**Staff page** (`http://localhost:8080/staff.html`)
- Add, edit, and delete vehicles: VIN, make, model, year, trim, body type,
  color, price, mileage, and status (Available, Reserved, Sold).
- See all test-drive bookings, filter them by status, and move each one from
  Requested to Confirmed to Completed, or cancel it.

**Business rules**
- A VIN is 17 letters and digits without I, O, or Q, and belongs to one vehicle
  (any letter case). A duplicate returns `409 Conflict`.
- Only available vehicles can be booked; reserved or sold ones return `409`.
- A booking can only move forward: Requested → Confirmed → Completed, or be
  cancelled before it is completed. Other changes return `409`.
- A vehicle with bookings cannot be deleted (`409`); mark it as sold instead.
- Invalid input returns `400` with an `errors` entry for each field.

## API

| Method | Path | Result |
|---|---|---|
| `GET` | `/api/vehicles?make=&bodyType=&maxPrice=&status=` | Vehicles; every filter is optional |
| `GET` | `/api/vehicles/makes` | Distinct makes, for the filter list |
| `GET` | `/api/vehicles/{id}` | One vehicle, or `404` |
| `POST` | `/api/vehicles` | Adds a vehicle: `201`, `400`, or `409` |
| `PUT` | `/api/vehicles/{id}` | Updates a vehicle: `200`, `400`, `404`, or `409` |
| `DELETE` | `/api/vehicles/{id}` | Deletes a vehicle: `204`, `404`, or `409` |
| `GET` | `/api/test-drives?status=` | Bookings, soonest first |
| `POST` | `/api/test-drives` | Books a test drive: `201`, `400`, `404`, or `409` |
| `PATCH` | `/api/test-drives/{id}/status` | Changes a booking's status: `200`, `400`, `404`, or `409` |

Body types: `SEDAN`, `SUV`, `HATCHBACK`, `TRUCK`, `COUPE`, `VAN`, `WAGON`,
`CONVERTIBLE`. Vehicle statuses: `AVAILABLE`, `RESERVED`, `SOLD`. Booking
statuses: `REQUESTED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`.

## Run it

Use Java 21 and a running MySQL server. In PowerShell, from this folder:

```powershell
$env:DB_USERNAME = "<your MySQL username>"
$env:DB_PASSWORD = "<your MySQL password>"
.\mvnw.cmd spring-boot:run
```

Then open `http://localhost:8080/`. By default the app uses a database named
`showroom` on `localhost:3306` and creates it if your MySQL user is allowed to.
Set `DB_URL` to use a different server or database. Flyway creates the tables
on first start.

**Sample vehicles.** To start with eight demo vehicles, add this before
starting the app:

```powershell
$env:FLYWAY_LOCATIONS = "classpath:db/migration,classpath:db/sample"
```

The sample script skips vehicles whose VIN already exists, so leaving it on is
safe.

## Tests

```powershell
.\mvnw.cmd test
```

The tests use an in-memory H2 database in MySQL mode and need no credentials.
`MySqlShowroomIntegrationTests` also runs the migrations, sample data, and API
against a temporary MySQL 8.4 container when Docker is running; without Docker
it is skipped. It never uses your own database.

## Project structure

```
src/main/java/com/example/showroom/
  vehicle/    Vehicle entity, request, repository, service, controller
  testdrive/  Test-drive entity, request/response, repository, service, controller
  web/        Error responses shared by both features
src/main/resources/
  db/migration/  Flyway schema migrations
  db/sample/     Optional sample inventory
  static/        Showroom and staff pages
```
