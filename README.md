markdown# SQL Query Agent

An agentic AI system that accepts plain English questions and returns answers from a real PostgreSQL database — by generating, validating, and executing SQL at runtime using an LLM.

Built to understand prompt chaining, tool safety, and the text-to-SQL pattern that powers most enterprise data AI products today.

## The problem it solves

Business teams need data answers but can't write SQL. Engineering teams get flooded with "can you pull this report" requests. Traditional BI tools require predefined queries — you can only ask questions someone anticipated.

This agent lets anyone ask any question in plain English and get a real answer from real data — without writing a single line of SQL.

## How it works
```
Plain English question
         ↓
System prompt injects table schema
         ↓
LLM understands intent → writes SQL query
         ↓
Safety check → only SELECT allowed, block everything else
         ↓
JdbcTemplate executes query on real PostgreSQL
         ↓
Raw result sent back to LLM
         ↓
LLM formats human readable answer
```

Each step is focused on one job. This is the prompt chaining pattern — breaking a complex task into sequential focused steps where each step gets full LLM attention.

## Real queries the agent handled
```
"How many orders do we have in total?"
→ SELECT COUNT(*) FROM orders
→ "We have 8 orders in total."

"Show me all orders from Bangalore"
→ SELECT * FROM orders WHERE city = 'Bangalore'
→ "There are 3 orders from Bangalore: Ravi Kumar ($1500, delivered)..."

"What is the total revenue from delivered orders?"
→ SELECT SUM(amount) FROM orders WHERE status = 'delivered'
→ "Total revenue from delivered orders is $4,050.00"

"Which city has the most pending orders?"
→ SELECT city, COUNT(*) FROM orders WHERE status = 'pending' 
   GROUP BY city ORDER BY COUNT(*) DESC LIMIT 1
→ "Mumbai has the most pending orders with 2."

"Who is our highest paying customer?"
→ SELECT customer_name, amount FROM orders ORDER BY amount DESC LIMIT 1
→ "Deepa Verma with $5,500.00"

"Delete all cancelled orders"
→ BLOCKED — only SELECT queries allowed
→ "I cannot delete orders. Only SELECT queries are permitted."
```

Zero SQL written by the developer. LLM generated every query above from plain English.

## Architecture decisions and tradeoffs

**Why JdbcTemplate over JPA?**
The LLM generates SQL dynamically at runtime — we don't know the query at compile time. JPA is built for object-relational mapping with fixed queries through entity classes. JdbcTemplate executes raw SQL with one line. Right tool for the job.

**Why inject schema into system prompt?**
LLMs hallucinate column names when they don't know the schema. Injecting exact column names and types eliminates hallucination on schema. The LLM knows `customer_name` exists — it won't guess `customerName` or `name`.

**Why block at the tool level not the prompt level?**
Telling the LLM "never write DELETE" in the prompt is not a security control — it's a suggestion. A sufficiently creative prompt can bypass it. Blocking in Java code (`if not SELECT → reject`) is a hard boundary the LLM cannot cross regardless of what it generates.

**The data privacy tradeoff**
Query results are sent to the LLM to form natural language answers. This means real data leaves your system and hits the LLM provider's servers. For production enterprise use, this requires either a private LLM deployment (AWS Bedrock inside VPC) or result masking before sending to LLM. This is the core tension in all text-to-SQL systems.

**Why not send all 500 tables?**
Context window limits make dumping an entire enterprise schema impossible. Production solution: Schema RAG — embed table descriptions as vectors, retrieve only relevant tables at query time, inject only those into the prompt. Next iteration of this project.

## What this demonstrates

- **Prompt chaining** — complex task decomposed into sequential focused steps
- **Text to SQL pattern** — how enterprise data AI products work under the hood
- **Tool safety** — hard boundaries in code, not soft suggestions in prompts
- **System prompt engineering** — schema injection to eliminate hallucination
- **Data privacy awareness** — understanding what leaves your system and when

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| AI integration | Spring AI 1.0 |
| LLM | Llama 3.2 via Groq API |
| Database | PostgreSQL |
| DB access | JdbcTemplate (raw SQL) |
| Build | Maven |

## Setup
```sql
CREATE DATABASE agentdb;
\c agentdb

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(100),
    city VARCHAR(50),
    amount DECIMAL(10,2),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO orders (customer_name, city, amount, status) VALUES
('Ravi Kumar', 'Bangalore', 1500.00, 'delivered'),
('Priya Singh', 'Mumbai', 2300.00, 'pending'),
('Arjun Mehta', 'Delhi', 890.00, 'delivered'),
('Sneha Rao', 'Bangalore', 4200.00, 'cancelled'),
('Kiran Patil', 'Pune', 670.00, 'delivered'),
('Anita Sharma', 'Mumbai', 3100.00, 'pending'),
('Rahul Nair', 'Bangalore', 990.00, 'delivered'),
('Deepa Verma', 'Delhi', 5500.00, 'pending');
```
```properties
# application.properties
spring.ai.openai.api-key=YOUR_GROQ_KEY
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.2-11b-text-preview
spring.datasource.url=jdbc:postgresql://localhost:5432/agentdb
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```
```bash
mvn spring-boot:run
```

## What's next

- Schema RAG — embed 500 table descriptions, retrieve relevant ones at query time
- Result masking — strip PII before sending to LLM
- Query validator — parse SQL, verify columns exist before executing
- AWS Bedrock deployment — keep data inside VPC for enterprise use
- Conversation memory — multi-turn queries that reference previous results
