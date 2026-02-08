# Task Manager API

A production-ready RESTful API for managing tasks, built with Spring Boot 3.5.10 and Java 21.

**Live Demo:** [https://tasks.dhanur.me](https://tasks.dhanur.me) (redirects to Swagger UI)
**API Documentation:** [https://tasks.dhanur.me/swagger-ui/index.html](https://tasks.dhanur.me/swagger-ui/index.html)
**Health Check:** [https://tasks.dhanur.me/actuator/health](https://tasks.dhanur.me/actuator/health)
**API Base URL:** `https://tasks.dhanur.me/api/v1`

## Features

- Full CRUD operations for task management
- Task status management (TODO, IN_PROGRESS, DONE)
- Pagination and sorting support
- Comprehensive input validation
- Global exception handling with detailed error responses
- RESTful API design with proper HTTP status codes
- OpenAPI/Swagger documentation
- H2 database (in-memory for dev, file-based for prod)
- Docker support with multi-stage builds
- CI/CD pipeline with GitHub Actions
- Health checks and monitoring with Spring Boot Actuator
- Comprehensive unit and integration tests

## Tech Stack

| Technology        | Version  | Purpose               |
| ----------------- | -------- | --------------------- |
| Java              | 21 (LTS) | Programming language  |
| Spring Boot       | 3.5.10   | Application framework |
| Spring Data JPA   | 3.5.10   | Data persistence      |
| H2 Database       | 2.4.x    | Embedded database     |
| Hibernate         | 7.2.x    | ORM                   |
| Lombok            | Latest   | Boilerplate reduction |
| SpringDoc OpenAPI | 2.8.15   | API documentation     |
| JUnit 5           | Latest   | Testing framework     |
| Maven             | 3.8+     | Build tool            |
| Docker            | Latest   | Containerization      |

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.8+ (or use included Maven wrapper)
- Docker (optional, for containerized deployment)

### Running Locally

```bash
# Clone the repository
git clone https://github.com/kascit/tasks.git
cd tasks

# Run with Maven wrapper
./mvnw spring-boot:run

# Or build and run the JAR
./mvnw clean package
java -jar target/tasks-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Running with Docker

```bash
# Build the Docker image
docker build -t task-manager-api .

# Run the container
docker run -p 8080:8080 task-manager-api

# Or use Docker Compose
docker-compose up
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TaskServiceTest

# Run with coverage
./mvnw verify
```

## API Endpoints

### Base URL

- Local: `http://localhost:8080/api/v1`
- Production: `https://tasks.dhanur.me/api/v1`

### Endpoints

| Method | Endpoint             | Description                                 |
| ------ | -------------------- | ------------------------------------------- |
| POST   | `/tasks`             | Create a new task                           |
| GET    | `/tasks`             | Get all tasks (with pagination & filtering) |
| GET    | `/tasks/{id}`        | Get a specific task by ID                   |
| PUT    | `/tasks/{id}`        | Update a task                               |
| PATCH  | `/tasks/{id}/status` | Update task status only                     |
| DELETE | `/tasks/{id}`        | Delete a task                               |

### Example Requests

**Create a Task**

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive README"
  }'
```

**Get All Tasks**

```bash
# Get all tasks with default pagination
curl http://localhost:8080/api/v1/tasks

# With custom pagination and sorting
curl "http://localhost:8080/api/v1/tasks?page=0&size=10&sort=createdAt,desc"

# Filter by status
curl "http://localhost:8080/api/v1/tasks?status=IN_PROGRESS"
```

**Get Task by ID**

```bash
curl http://localhost:8080/api/v1/tasks/1
```

**Update Task**

```bash
curl -X PUT http://localhost:8080/api/v1/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated title",
    "description": "Updated description"
  }'
```

**Update Task Status**

```bash
curl -X PATCH "http://localhost:8080/api/v1/tasks/1/status?status=DONE"
```

**Delete Task**

```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/1
```

## API Documentation

Interactive API documentation is available via Swagger UI:

- Local: [http://localhost:8080](http://localhost:8080) or [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Production: [https://tasks.dhanur.me](https://tasks.dhanur.me) or [https://tasks.dhanur.me/swagger-ui/index.html](https://tasks.dhanur.me/swagger-ui/index.html)

## Configuration

### Application Profiles

The application supports multiple Spring profiles:

**Development (`dev`)**

- H2 in-memory database
- SQL logging enabled
- H2 console enabled at `/h2-console`
- Debug logging

**Production (`prod`)**

- H2 file-based database (persisted to `./data/taskdb`)
- SQL logging disabled
- H2 console disabled
- Info-level logging

Set the active profile:

```bash
# Via environment variable
export SPRING_PROFILES_ACTIVE=prod

# Via command line
java -jar app.jar --spring.profiles.active=prod

# Via Docker
docker run -e SPRING_PROFILES_ACTIVE=prod task-manager-api
```

### Environment Variables

| Variable                 | Description           | Default |
| ------------------------ | --------------------- | ------- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev`   |
| `PORT`                   | Server port           | `8080`  |

## Database

### H2 Database Console (Dev Only)

When running in `dev` profile, access the H2 console at:

- URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: (leave empty)

### Schema

**Task Table**

```sql
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Project Structure

```
src/
├── main/
│   ├── java/me/dhanur/tasks/
│   │   ├── TasksApplication.java       # Main application class
│   │   ├── config/
│   │   │   └── OpenApiConfig.java      # Swagger/OpenAPI configuration
│   │   ├── controller/
│   │   │   └── TaskController.java     # REST API endpoints
│   │   ├── dto/
│   │   │   ├── TaskRequest.java        # Request DTO
│   │   │   ├── TaskResponse.java       # Response DTO
│   │   │   └── ErrorResponse.java      # Error response DTO
│   │   ├── entity/
│   │   │   ├── Task.java               # JPA entity
│   │   │   └── TaskStatus.java         # Status enum
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java    # Global exception handling
│   │   │   └── ResourceNotFoundException.java # Custom exception
│   │   ├── repository/
│   │   │   └── TaskRepository.java     # JPA repository
│   │   └── service/
│   │       ├── TaskService.java        # Service interface
│   │       └── TaskServiceImpl.java    # Service implementation
│   └── resources/
│       ├── application.yaml            # Main configuration
│       ├── application-dev.yaml        # Dev profile config
│       └── application-prod.yaml       # Prod profile config
└── test/
    └── java/me/dhanur/tasks/
        ├── TasksApplicationTests.java  # Context load test
        ├── controller/
        │   └── TaskControllerIntegrationTest.java  # Controller tests
        └── service/
            └── TaskServiceTest.java    # Service unit tests
```

## Monitoring & Health Checks

### Actuator Endpoints

| Endpoint            | Description               |
| ------------------- | ------------------------- |
| `/actuator/health`  | Application health status |
| `/actuator/info`    | Application information   |
| `/actuator/metrics` | Application metrics       |

Example:

```bash
curl http://localhost:8080/actuator/health
```

## Deployment

### Deploy with Docker

```bash
# Build
docker build -t task-manager-api .

# Run
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v $(pwd)/data:/app/data \
  --name task-manager-api \
  task-manager-api
```

### Deploy to Render

1. Push code to GitHub
2. Create new Web Service on [Render](https://render.com)
3. Connect GitHub repository
4. Configure:
   - **Build Command:** `./mvnw clean package -DskipTests`
   - **Start Command:** `java -jar target/tasks-0.0.1-SNAPSHOT.jar`
   - **Environment:** `SPRING_PROFILES_ACTIVE=prod`
5. Deploy

## Testing

### Test Coverage

- Unit Tests: 11 tests for service layer
- Integration Tests: 14 tests for controller layer
- Total: 26 tests

Run tests:

```bash
# All tests
./mvnw test

# With coverage report
./mvnw verify

# Specific test class
./mvnw test -Dtest=TaskServiceTest
```

## CI/CD

The project includes a GitHub Actions workflow that:

- Runs on push to `main` or `develop` branches
- Compiles the code
- Runs all tests
- Generates test reports
- Packages the application
- Builds Docker image
- Tests the Docker image

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Contact

**Developer:** kascit
**Email:** [contact@dhanur.me](mailto:contact@dhanur.me)
**GitHub:** [github.com/kascit](https://github.com/kascit)
**Project:** [github.com/kascit/tasks](https://github.com/kascit/tasks)

## Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- API Documentation powered by [SpringDoc OpenAPI](https://springdoc.org/)
- Containerization with [Docker](https://www.docker.com/)
