# Legacy notes: business rules, examples, smells, target design

## 1. Business rules (the behavior to preserve)

**Customers**
- Register: name required; email must contain `@` (not at position 0); email unique (case-sensitive lookup,
  stored lower-cased); credit limit defaults to 10000.00 when not positive; new customers are `ACTIVE`.
- Only `ACTIVE` customers can place orders.

**Orders** (`OrderService.placeOrder`)
1. At least one item; every product must exist and be active; quantity must be positive.
2. `lineTotal = round2(unitPrice × qty)`; `subtotal = round2(sum of line totals)`.
3. Discount on the subtotal: **> 5000 → 10%**, **> 1000 → 5%**, otherwise 0 (boundaries are exclusive).
4. `tax = round2((subtotal − discount) × tax.rate)` with `tax.rate = 0.18`; `total = round2(subtotal − discount + tax)`.
5. Credit check: `outstanding invoices + total` must not exceed the customer's credit limit
   (outstanding = unpaid balance of `OPEN` and `OVERDUE` invoices).
6. Order + items are inserted and stock reserved **in one database transaction**; if stock is short the
   whole thing rolls back. Order numbers look like `ORD-yyyyMMdd-NNNN`.
7. After commit, an invoice is created in a **separate** transaction (due in 30 days, status `OPEN`).
   If that fails, the error is only logged and the order stays.
- `round2(v) = Math.round(v * 100.0) / 100.0` on `double`.
- Cancel: only `NEW` or `CONFIRMED`; stock is returned; status becomes `CANCELLED`.

**Billing**
- Payment must be positive; invoice must exist and not be `PAID`; method defaults to `CASH`.
- Posting is done by the `apply_payment` procedure: locks the invoice, rejects overpayment, inserts the payment,
  updates the invoice (`PAID` when fully paid) and commits itself.
- `runOverdue` (ADMIN) turns `OPEN` invoices past their due date into `OVERDUE`.

**Catalog / inventory**
- Only ADMIN can change prices; every price change is logged by a trigger.
- Stock cannot go below zero (`adjust_stock` procedure raises "Insufficient stock").

**Security**
- Session login with MD5 password hashes; session timeout 30 min; `/api/*` requires a session.

## 2. Worked examples (use as characterization tests)
Seed data: customer 1 Asha (limit 50000), customer 2 Bharat (limit 5000), customer 3 Chandra (SUSPENDED);
products 1 KB-100 @600 (stock 100), 2 MS-200 @250 (200), 3 MN-300 @9000 (10), 4 LP-400 @800 (50), 5 CB-500 @150 (500), 6 inactive.

| # | Request | Expected |
|---|---|---|
| A | customer 1, `1:2` (2 × 600) | 201; subtotal 1200.0, discount 60.0, tax 205.2, **total 1345.2** |
| B | customer 1, `1:10` | subtotal 6000.0, discount 600.0, tax 972.0, **total 6372.0** |
| C | customer 1, `2:4` (4 × 250 = 1000 exactly) | no discount (boundary): tax 180.0, **total 1180.0** |
| D | customer 2, `3:1` (total 9558.0 > limit 5000) | 409 `Credit limit exceeded` |
| E | customer 1, `4:51` (total 43329.6, passes credit, stock is 50) | 409 `Could not place order: Insufficient stock`; **no order row**, stock unchanged; an order number was still consumed (gap) |
| F | customer 3 (suspended), any item | 409 `Customer is not active: 3` |
| G | customer 1, item `6:1` (inactive product) | 400 `Invalid product: 6` |
| H | pay invoice of order A: 500, then 845.2, then 1 more | OPEN → PAID, then 409 `Invoice already paid` |
| I | pay more than the balance | 409 `Payment failed: Payment exceeds invoice balance` |
| J | cancel a `NEW` order | status `CANCELLED`, stock returned, one `order_audit` row (trigger); **its invoice stays OPEN** |

## 3. Known quirks (decide: preserve, fix, or ask the business)
- A cancelled order's invoice stays `OPEN` and still counts against the customer's credit.
- If invoice creation fails after an order commits, the order exists without an invoice (error only logged).
- The order-number counter is in memory: it restarts on redeploy and is not safe across several instances.
- A consumed order number is not reused when a placement rolls back (gaps).
- Money is `double` in Java but `DECIMAL(12,2)` in the database; rounding can differ from `BigDecimal`.
- The discount/tax rules live in Java; the "outstanding balance" rule lives in **both** Java and `fn_customer_outstanding`.
- `product` search and `order` search are vulnerable to SQL injection (see smells).

## 4. Intentional legacy smells (what the migration should address)
| Smell | Where |
|---|---|
| Direct `DriverManager` fallback, JNDI lookup, credentials in a properties file | `common/DBUtil`, `db.properties` |
| Singletons and static mutable state | `DBUtil`, `CustomerService`, `OrderService`, `OrderNumberGenerator.counter`, `DateUtil.DAY` (non thread-safe formatter) |
| SQL built by string concatenation (injection) | `UserDao`, `CustomerDao.findByEmail/searchByName`, `ProductDao.listByCategory`, `OrderDao.search`, `SalesReportDao.topCustomers` |
| Manual transaction shared across modules (order + inventory on one connection) | `OrderService.placeOrder/cancelOrder`, `InventoryDao.adjustStock` |
| Business rules inside stored procedures, including their own `COMMIT` | `apply_payment`, `adjust_stock`, `mark_overdue_invoices` |
| Triggers carrying behavior (audit, price history, integrity) | `sql/03_triggers.sql` |
| Duplicated logic in Java and SQL | `InvoiceDao.outstandingFor` vs `fn_customer_outstanding` |
| EJB with transaction attributes, instantiated with `new` | `billing/InvoiceBean`, `OrderService` |
| Circular dependency between modules | `order` ↔ `billing` |
| A view spanning several modules' tables | `v_sales_summary` used by `report` |
| Session state and role checks in servlets | `LoginServlet`, `ProductServlet`, `BillingServlet` |
| MD5 password hashing | `StringUtil.md5`, `users.password_md5` |
| Errors swallowed (`printStackTrace`, return null / -1 / 0) | all DAOs |
| `System.out` logging, local file output | services, `ReportServlet.export` |
| Hand-built JSON, no DTOs, no validation framework | all servlets |
| No tests | whole project |

## 5. What a sensible target looks like
Running the migrator's analysis on this project proposes roughly these services:

| Service | Owns | Notes |
|---|---|---|
| auth | `users` | replace sessions with token-based auth; replace MD5 with a modern password hash (requires a rehash/reset plan) |
| customer | `customers` | simplest first slice; few dependencies |
| product (+ inventory) | `products`, `product_price_history`, `inventory` | `inventory` is tightly coupled to `product`; consider one "catalog" service |
| order | `orders`, `order_items`, `order_audit` | needs customer, catalog and billing; stock reservation can no longer share a DB transaction |
| billing | `invoices`, `payments` | circular with order: decide who owns "create invoice after order" (events are the usual answer) |
| report | none (read model) | reads data spanning customers and orders: API calls or a replicated read model |
| `common` | none | a shared library, not a service: becomes Spring configuration |

Decisions the plan will force you to make: how stock reservation and order creation stay consistent
(saga/outbox), how `order` and `billing` break their cycle, how the report gets its data, what happens to
triggers and stored procedures (service logic, JPA auditing, or keep in the DB), and how users migrate to new
password hashing.

Suggested order: customer, then catalog (product + inventory), then billing/order together, then report, with
auth handled early because everything depends on it.
