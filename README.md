# vertx-crud

[![Java CI with Maven](https://github.com/hendisantika/vertx-crud/actions/workflows/maven.yml/badge.svg)](https://github.com/hendisantika/vertx-crud/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Vert.x](https://img.shields.io/badge/Vert.x-5.x-purple?logo=eclipsevertdotx)
![Maven](https://img.shields.io/badge/Maven-3.9-blue?logo=apachemaven)
![Dependabot](https://img.shields.io/badge/Dependabot-enabled-brightgreen?logo=dependabot)

Simple REST CRUD API for users built with **Eclipse Vert.x 5** (Vert.x Web + Reactive MySQL Client) on **JDK 25**.

## Tech Stack

| Component            | Version |
|----------------------|---------|
| Java (Temurin)       | 25      |
| Eclipse Vert.x       | 5.x     |
| Vert.x MySQL Client  | 5.x (reactive, non-blocking) |
| Logback              | 1.6.x   |
| JUnit Jupiter        | 6.x     |
| Maven (wrapper)      | 3.9.x   |

## Prerequisites

- JDK 25
- MySQL 8.x (or Docker)

## Database Setup

Run MySQL with Docker:

```bash
docker run -d --name mysql-vertx \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=testdb \
  -p 3306:3306 mysql:8
```

Create the table:

```sql
CREATE TABLE usert (
  username VARCHAR(100) PRIMARY KEY,
  email    VARCHAR(255) NOT NULL
);
```

## Configuration

Settings are read from environment variables (defaults shown):

| Variable      | Default     |
|---------------|-------------|
| `DB_HOST`     | `localhost` |
| `DB_PORT`     | `3306`      |
| `DB_NAME`     | `testdb`    |
| `DB_USER`     | `root`      |
| `DB_PASSWORD` | `root`      |
| `HTTP_PORT`   | `8080`      |

## Build & Run

```bash
# Run tests
./mvnw clean test

# Package (creates target/vertx-crud-1.0.0-SNAPSHOT-fat.jar)
./mvnw clean package

# Run with Maven
./mvnw clean compile exec:java

# Or run the fat jar
java -jar target/vertx-crud-1.0.0-SNAPSHOT-fat.jar
```

## API Endpoints

| Method   | Endpoint             | Description          |
|----------|----------------------|----------------------|
| `GET`    | `/users`             | List all users       |
| `GET`    | `/users/{username}`  | Get user by username |
| `POST`   | `/users`             | Create a new user    |
| `PUT`    | `/users/{username}`  | Update user email    |
| `DELETE` | `/users/{username}`  | Delete a user        |

### Examples

```bash
# Create
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"hendi","email":"hendisantika@gmail.com"}'

# List all
curl http://localhost:8080/users

# Get one
curl http://localhost:8080/users/hendi

# Update
curl -X PUT http://localhost:8080/users/hendi \
  -H "Content-Type: application/json" \
  -d '{"email":"hendi@example.com"}'

# Delete
curl -X DELETE http://localhost:8080/users/hendi
```

## CI/CD & Dependency Updates

- **GitHub Actions** (`.github/workflows/maven.yml`) builds and tests the project with JDK 25 on every push and pull request to `main`, then submits the dependency graph.
- **Dependabot** (`.github/dependabot.yml`) checks Maven and GitHub Actions dependencies weekly (Monday 05:00 Asia/Jakarta), grouping related updates (Vert.x, Maven plugins, test, logging, actions) into single PRs.

## Help

- [Vert.x Documentation](https://vertx.io/docs/)
- [Vert.x 5 Migration Guide](https://vertx.io/docs/guides/vertx-5-migration-guide/)
- [Vert.x Discord](https://discord.gg/6ry7aqPWXy)

## Author

**Hendi Santika** — [s.id/hendisantika](https://s.id/hendisantika) · Telegram [@hendisantika34](https://t.me/hendisantika34)
