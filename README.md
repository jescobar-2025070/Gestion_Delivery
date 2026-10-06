# FastOrder — Backend (entrega inicial, Fase 1)

Backend de comercio electrónico y delivery rápido basado en microservicios.
**Estado:** estructura base. Aún no hay endpoints, entidades ni lógica de negocio (Fases 2–8).

## Requisitos previos

- Java 21
- Maven 3.9+
- MySQL 8+
- Git

## Stack

Spring Boot 3.4.5 · Spring Security (stateless) · Spring Data JPA · MySQL · jjwt 0.12.6 · BCrypt · Lombok · SSE (planificado).

## Estructura

```
fastorder/
├── pom.xml                 # POM padre (Spring Boot parent, versiones)
├── common/                 # Librería compartida
│   └── src/main/
│       ├── java/com/fastorder/common/
│       │   ├── config/     # JwtProperties, CorsProperties, CommonSecurityConfig (BCrypt, CORS)
│       │   ├── security/   # StatelessSecuritySupport (base stateless)
│       │   ├── domain/     # Rol, AppConstants
│       │   └── exception/  # ErrorResponse, InsufficientStock/ResourceNotFound/InvalidStatus
│       └── resources/fastorder-common.yml   # datasource, Hikari, JPA, JWT, CORS (compartido)
├── auth-service/      :8081
├── catalog-service/   :8082
├── order-service/     :8083
└── delivery-service/  :8084
```

Cada servicio tiene los paquetes `controller`, `service`, `repository`, `entity`, `dto`, `security`, `exception`, `config`
y comparte configuración mediante `spring.config.import: classpath:fastorder-common.yml`.

**Persistencia:** todos los servicios usan el mismo esquema MySQL (`fastorder`) para garantizar la transacción ACID
de pedido + stock; la separación entre servicios es lógica (cada uno es dueño de sus entidades).

## Configuración

1. Crear la base de datos y el usuario:

```sql
CREATE DATABASE fastorder CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'fastorder'@'%' IDENTIFIED BY 'tu-password';
GRANT ALL PRIVILEGES ON fastorder.* TO 'fastorder'@'%';
```

2. Variables de entorno (ver `.env.example`; copiarlo a `.env`, que está en `.gitignore`):

| Variable | Obligatoria | Default |
|---|---|---|
| `DB_PASSWORD` | sí | — |
| `JWT_SECRET` (≥ 32 caracteres) | sí | — |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` | no | localhost / 3306 / fastorder / fastorder |
| `JWT_EXPIRATION_MS` | no | 3600000 |
| `CORS_ALLOWED_ORIGINS` | no | http://localhost:3000 |
| `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` | no | 20 / 5 |
| `*_SERVICE_PORT` | no | 8081–8084 |

## Compilar y ejecutar

```bash
git clone <repo> && cd fastorder
set -a; source .env; set +a
mvn clean package            # compila los 5 módulos

java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar
java -jar catalog-service/target/catalog-service-0.1.0-SNAPSHOT.jar
java -jar order-service/target/order-service-0.1.0-SNAPSHOT.jar
java -jar delivery-service/target/delivery-service-0.1.0-SNAPSHOT.jar
```

O con Maven: `mvn -pl auth-service -am spring-boot:run`.

## Hoja de ruta

2. Autenticación y seguridad (JWT, RBAC) · 3. Comercios y productos · 4. Pedidos y stock ·
5. Cancelación y estados · 6. Delivery y SSE · 7. Auditoría y calidad · 8. Pruebas finales y `test-fastorder.sh`.

Los endpoints, flujo de pedidos, SSE, credenciales de desarrollo y pruebas se documentarán al implementarse.
