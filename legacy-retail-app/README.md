# Acme Retail (legacy)

A deliberately "classic" Java web application meant to be **migrated to Spring Boot microservices**
(for example with the `java-sql-migrator` Claude Code plugin). It is small enough to understand in an
afternoon and messy enough to behave like real legacy code.

| | |
|---|---|
| Language / build | Java 8, Maven, WAR |
| Web | Servlets + a servlet filter (no framework), hand-built JSON |
| Business layer | Plain service classes with singletons, one stateless EJB |
| Data access | Plain JDBC (`Statement`, `PreparedStatement`, `CallableStatement`) |
| Database | MySQL 8 with stored procedures, a function, triggers and a view |
| Tests | None (as in many legacy systems) |

## Domain
Customers place orders for products. Stock is reserved when an order is placed, an invoice is created
(due in 30 days) and customers pay invoices. Reports summarize sales. Users log in with a session.

## Layout
```
legacy-retail-app/
├── pom.xml
├── sql/                       01_schema, 02_procedures, 03_triggers, 04_views, 05_seed (run in order)
├── src/main/java/com/acme/retail/
│   ├── common/      AppConfig, DBUtil, StringUtil, DateUtil          (shared helpers)
│   ├── auth/        UserVO, UserDao, LoginServlet, AuthFilter
│   ├── customer/    CustomerVO, CustomerDao, CustomerService, CustomerServlet
│   ├── product/     ProductVO, ProductDao, ProductServlet
│   ├── inventory/   InventoryDao, InventoryService
│   ├── order/       OrderVO, OrderItemVO, OrderDao, OrderService, OrderNumberGenerator, OrderServlet
│   ├── billing/     InvoiceVO, InvoiceDao, PaymentDao, InvoiceBean (EJB), BillingServlet
│   └── report/      SalesReportDao, ReportServlet
├── src/main/resources/db.properties
├── src/main/webapp/WEB-INF/web.xml
└── docs/LEGACY_NOTES.md       business rules, worked examples, known smells, expected target design
```

## Run it (optional)
You do not need to run it to migrate it, but running it lets you compare old and new behavior.
1. Create the database: run the files in `sql/` in order on MySQL 8 (the seed script creates sample data).
2. Edit `src/main/resources/db.properties` (or define a JNDI datasource `jdbc/RetailDS`).
3. `mvn package`, then deploy `target/retail.war` to Tomcat 9 (the EJB is instantiated directly with `new`,
   so a plain servlet container is enough). Context path: `/retail`.

Seed logins: `admin / admin123` (ADMIN) and `clerk / clerk123` (USER).

Smoke test:
```
curl -c jar -d "username=admin&password=admin123" http://localhost:8080/retail/login
curl -b jar "http://localhost:8080/retail/api/products?action=list"
curl -b jar -d "action=place&customerId=1&items=1:2" http://localhost:8080/retail/api/orders
```
(expected: HTTP 201, subtotal 1200.0, discount 60.0, tax 205.2, total 1345.2)

> **Status of this project:** all 30 Java classes were compile-checked at Java 8 source level against API stubs.
> It has **not** been built with Maven or run against MySQL/Tomcat in the environment where it was created,
> and the SQL has not been executed on a real MySQL server. Expect to fix small environment issues on first run.

## API (all under `/retail`, all except `/login` need a session)
| Endpoint | Operations |
|---|---|
| `POST /login` | `username`, `password` |
| `/api/customers` | GET `action=get&id=` · `action=search&name=` · POST `action=register` (`name,email,phone,creditLimit`) · `suspend` · `activate` (`id`) |
| `/api/products` | GET `action=get&id=` · `action=list&category=` · POST `action=updatePrice&id=&price=` (ADMIN) |
| `/api/orders` | GET `action=get&id=` · `action=search&status=&from=&to=&customerId=` · POST `action=place&customerId=&items=productId:qty,...` · `action=cancel&id=` |
| `/api/billing` | GET `id=` · POST `action=pay&id=&amount=&method=` · `action=runOverdue` (ADMIN) |
| `/api/reports` | GET `action=summary&from=&to=` · `action=top&limit=` · `action=export&from=&to=` |

Errors: 400 bad input, 401 not logged in, 403 not admin, 404 not found, 409 business-rule violation.

## Migrating it
Read `docs/LEGACY_NOTES.md` first. With the plugin installed, open this folder as the workspace and run:
```
/java-sql-migrator:migrate .
```
Start with a small slice (for example `--module customer`), review the plan before approving it, and use
the worked examples in the notes as characterization tests for the new services.
