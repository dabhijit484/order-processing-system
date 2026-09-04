# Local PostgreSQL Database Scripts

The backend expects a PostgreSQL database named `order_processing` on `localhost:5432`.

Run the scripts in this order:

1. Connect to the default `postgres` database as a PostgreSQL administrator and run `01_create_database.sql` once.
2. Connect to the new `order_processing` database and run `02_create_schema.sql`.

For `psql` on Windows, from the repository root:

```powershell
psql -U postgres -d postgres -f database/01_create_database.sql
psql -U postgres -d order_processing -f database/02_create_schema.sql
```

The default service connection details are in `order-process-service/src/main/resources/application.yml`:

- host: `localhost`
- port: `5432`
- database: `order_processing`
- username: `postgres`
- password: `postgres`

If your local PostgreSQL password differs, update `spring.datasource.password` in `application.yml` before starting the backend.

Note: The application also manages this schema through Flyway at `order-process-service/src/main/resources/db/migration/V1__create_orders_tables.sql`. The local application configuration has `baseline-on-migrate: true`, so after you run `02_create_schema.sql`, Flyway creates its schema history table and records version `1` without attempting to recreate the existing tables.

For a completely fresh database, you may instead let Flyway apply the migration automatically when the service starts.
