# SQL Query Agent

A simple Spring Boot agent that uses Spring AI (OpenAI-compatible tooling) and Postgres to answer user questions by generating SQL queries and executing them safely.

## Project Structure

- `src/main/java/org/example/SqlAgentApp.java` - Bootstraps the chat agent and runs example queries.
- `src/main/java/org/example/OrderTools.java` - Defines the `runQuery` tool with a SELECT-only safety guard.
- `src/main/java/org/example/DataSourceConfig.java` - Exposes `JdbcTemplate`.
- `src/main/resources/application.properties` - Configures OpenAI/Groq and PostgreSQL.

## Prerequisites

- Java 17+
- Maven
- PostgreSQL database with an `agentdb` database and `orders` table.
- OpenAI API key (or compatible endpoint key).

## Setup

1. Clone this repository and navigate into it.
2. Create Postgres DB and table:

```sql
CREATE DATABASE agentdb;
\c agentdb
CREATE TABLE orders (
  id SERIAL PRIMARY KEY,
  customer_name TEXT,
  city TEXT,
  amount NUMERIC,
  status TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO orders (customer_name, city, amount, status) VALUES
('Alice', 'Bangalore', 120.5, 'delivered'),
('Bob', 'Mumbai', 75.5, 'pending'),
('Carol', 'Bangalore', 420, 'delivered'),
('Dave', 'Delhi', 199.99, 'pending');
```

3. Set environment variables:

```bash
export OPENAI_API_KEY="<your_api_key>"
```

4. Build and run:

```bash
mvn clean package
mvn spring-boot:run
```

## Usage

The agent runs example questions at startup and prints user prompts and agent responses. It uses the `runQuery` tool to only execute `SELECT` queries on the orders table.

### Supported query domain
- Orders table columns: `id`, `customer_name`, `city`, `amount`, `status`, `created_at`.
- The tool only allows `SELECT` statements.

## Notes

- `Delete all cancelled orders` is intentionally blocked by the SELECT-only guard.
- To make database writes possible, add dedicated write tools with explicit safety checks.

## Deploy

Set the proper `spring.datasource.*` values in `application.properties` or environment variables, then run the Spring Boot application.

## License

MIT
