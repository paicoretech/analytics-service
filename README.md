# Analytics Service – Open Source Version

The Analytics Service by PAiCore Technologies is a Java Spring Boot REST API designed to query, process, and serve telecom network analytics data. It powers the ReCap dashboard with protocol-level insights, flow diagrams, DWR monitoring, and PCAP download capabilities.

This open source version allows you to deploy and run the analytics layer on top of your PostgreSQL database, with flexible configuration for telecom and enterprise-grade use cases.

---

## 🚀 Deploying the Analytics Service

### Prerequisites

- Java 11+
- Maven 3.6+
- PostgreSQL 12+
- Python 3 (for PCAP utility scripts)
- Ubuntu 22.04 (recommended)

> 💡 Ensure PostgreSQL allows remote connections and has proper user permissions.

---

### Java Service

Download or build the JAR and configure the systemd service to run it on startup.

Example systemd unit file:

```
[Unit]
Description=Analytics Service
After=network.target

[Service]
ExecStart=/opt/paic/analytics/analytics_service.sh

[Install]
WantedBy=multi-user.target
```

---

### PostgreSQL Setup

The Analytics Service connects to the same PostgreSQL databases used by the ingestion pipeline:

- `nbm` → raw trace data
- `ingestion` → processed trace metadata

Ensure your PostgreSQL server accepts remote connections.
See: [Configure PostgreSQL for remote access](https://www.postgresql.org/docs/current/auth-pg-hba-conf.html).

Create a dedicated user and grant appropriate privileges:

```sql
CREATE USER nbm_user WITH PASSWORD 'your_secure_password';
GRANT CONNECT ON DATABASE nbm TO nbm_user;
GRANT CONNECT ON DATABASE ingestion TO nbm_user;
```

Run the schema scripts from [db-scripts](https://github.com/paic/db-scripts) before starting this service.

---

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/nbm
spring.datasource.username=nbm_user
spring.datasource.password=your_secure_password

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Logging
logging.level.root=INFO
logging.file.name=/opt/analytics-service/log.log
```

---

### Build & Run

```bash
# Build
mvn clean install

# Run with Maven
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/analytics-service-*.jar
```

---

## Features

- 📡 **Protocol Analytics** — Diameter, GTP, HTTP, HTTP OCS, HTTP SS7, SIP, SMPP, SS7 CAP, SS7 MAP, Camel
- 📊 **Consolidated Analytics** — cross-protocol views and grouped summaries
- 🔁 **Flow Diagram Generator** — generates signalling sequence diagrams from trace data
- 📥 **PCAP Download Queue** — async job queue to generate and download filtered PCAP files
- 🛡️ **DWR Monitor** — real-time Diameter Watchdog Request monitoring and alarms
- 🌐 **IP Names API** — maps IP addresses to named nodes
- 🔗 **External API Integration** — IMSI/MSISDN lookups via external requests

---

## License

This project is released under the GNU GPL v3 license.

---

## Community and Support

This open source version is maintained by the PAiCore Technologies team and the community.
For commercial support, enterprise deployments, or custom solutions, please contact us at:

🌐 https://paicore.tech
