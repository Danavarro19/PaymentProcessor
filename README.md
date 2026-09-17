# Payment Processor

Servicio de integración desarrollado con Java y Spring Boot para recibir pagos en formato JSON, transformarlos al formato XML requerido por un sistema core legado y persistir el resultado de forma simulada.

El proyecto también incluye scripts SQL/PLSQL para procesamiento y análisis de pagos, autenticación mediante JWT y un script Python para generar reportes CSV a partir de la API.

## Tecnologías utilizadas

- Java 17
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- Spring Security
- JWT (JJWT)
- H2 Database
- Jackson XML
- Springdoc OpenAPI / Swagger UI
- Gradle
- JUnit 5
- Mockito
- Python 3
- Oracle SQL / PL/SQL

## Requisitos

Para ejecutar el proyecto se necesita:

- Java 17 o superior
- Python 3 para ejecutar el script de reportes

No es necesario instalar Gradle, ya que el repositorio incluye Gradle Wrapper.

El script Python utiliza únicamente módulos de la librería estándar de Python, por lo que no requiere instalar dependencias adicionales.

## Ejecución del proyecto

Clonar el repositorio:

```bash
git clone https://github.com/Danavarro19/PaymentProcessor.git
cd PaymentProcessor
```

Ejecutar la aplicación:

```bash
./gradlew bootRun
```

En Windows:

```bash
gradlew.bat bootRun
```

Por defecto, la API queda disponible en:

```text
http://localhost:8080
```

La aplicación utiliza una base H2 en memoria, por lo que los datos se eliminan al detener la aplicación.

## Autenticación

Los endpoints de pagos están protegidos mediante JWT.

Para facilitar la evaluación del proyecto se crea automáticamente un usuario al iniciar la aplicación:

```text
Usuario: admin@payments.com
Contraseña: admin123
```

Estas credenciales y el secreto JWT se encuentran configurados directamente en `application.properties` únicamente para simplificar la ejecución de esta prueba técnica.

En un ambiente productivo estos valores no deberían almacenarse en el repositorio. Se utilizarían variables de entorno o un sistema de gestión de secretos.

### Obtener un token

```http
POST /auth/login
Content-Type: application/json
```

Ejemplo:

```json
{
  "email": "admin@payments.com",
  "password": "admin123"
}
```

La respuesta contiene un JWT:

```json
{
  "token": "..."
}
```

Los endpoints protegidos requieren posteriormente:

```text
Authorization: Bearer <token>
```

### ¿Por qué JWT?

Se eligió JWT porque permite mantener la API sin estado y separar el proceso de autenticación de los endpoints de negocio.

Para el alcance de esta prueba se implementó únicamente autenticación mediante login y validación del token. No se implementaron refresh tokens, revocación de tokens ni administración de usuarios.

## API de pagos

### Crear un pago

```http
POST /payments
```

Ejemplo:

```json
{
  "id": "PAY-1001",
  "customerId": "CUST-10",
  "amount": 125.50,
  "currency": "USD",
  "timestamp": "2026-09-16T10:00:00-06:00"
}
```

La API valida los campos de entrada y devuelve errores con una estructura consistente cuando la solicitud no es válida.

Un pago procesado correctamente queda con estado:

```text
PROCESSED
```

Si ocurre un error durante la transformación o escritura del XML, el pago se registra con estado:

```text
FAILED
```

También se evita procesar dos veces un pago con el mismo identificador.

### Consultar pagos

```http
GET /payments
```

Los filtros son opcionales:

```text
customerId
from
to
```

Ejemplo:

```http
GET /payments?customerId=CUST-10&from=2026-09-01T00:00:00Z&to=2026-09-30T23:59:59Z
```

El rango de fechas se valida y se devuelve un error `400 Bad Request` cuando `from` es posterior a `to`.

## Integración JSON → XML

El escenario plantea que el sistema core solamente acepta XML.

Por este motivo, el pago recibido como JSON se transforma a un DTO específico para la integración y posteriormente se serializa a XML.

El envío al sistema core se simuló persistiendo cada XML en disco.

Los archivos se generan en:

```text
outbox/
```

con nombres como:

```text
payment-PAY-1001.xml
```

Se eligió persistencia en disco porque permite simular de manera simple y verificable la entrega hacia un sistema externo sin introducir infraestructura adicional que no forma parte del alcance de la prueba.

La escritura también valida el identificador utilizado para construir el nombre del archivo para evitar que pueda utilizarse para escribir fuera del directorio configurado.

En una implementación real, esta capa podría reemplazarse por una integración HTTP, mensajería o el mecanismo utilizado por el sistema core sin modificar la lógica principal del servicio.

## Manejo de errores

La API utiliza un manejador global de excepciones para mantener respuestas de error consistentes.

Entre los escenarios manejados se encuentran:

- errores de validación
- pagos duplicados
- rangos de fecha inválidos
- errores durante el procesamiento o persistencia del XML
- solicitudes sin autenticación válida

La estructura general de los errores de negocio contiene:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "...",
  "message": "...",
  "path": "/payments"
}
```

## Swagger / OpenAPI

La documentación OpenAPI se genera automáticamente utilizando Springdoc.

Con la aplicación ejecutándose, Swagger UI está disponible en:

```text
http://localhost:8080/swagger-ui/index.html
```

La especificación OpenAPI generada puede consultarse en:

```text
http://localhost:8080/v3/api-docs
```

## Collection de Postman

El repositorio incluye una colección de Postman para probar manualmente:

- autenticación
- creación de pagos
- consulta de pagos
- filtros
- validaciones
- pagos duplicados
- acceso sin autenticación

La solicitud de login guarda automáticamente el JWT en una variable de la colección para utilizarlo en las siguientes solicitudes.

## Persistencia

Para el servicio se utiliza H2 en memoria.

Esta decisión permite ejecutar la solución sin instalar o configurar una base de datos externa y es suficiente para el alcance de la prueba.

Los datos se recrean cada vez que se inicia la aplicación.

Oracle no es necesario para ejecutar el servicio. Los ejercicios SQL y PL/SQL solicitados se entregan por separado como scripts listos para revisión.

## SQL y PL/SQL

Los scripts sql se encuentran en:

```text
sql/
```

Incluyen:

- consulta de los 10 clientes con mayor monto pagado durante los últimos 30 días, incluyendo cantidad de pagos y ticket promedio
- stored procedure para procesar pagos en estado `PENDING`
- registro de errores durante el procesamiento
- índice propuesto para mejorar la consulta analítica

Los scripts asumen el modelo especificado en la prueba:

```text
CUSTOMERS (ID, NAME, COUNTRY)
PAYMENTS (ID, CUSTOMER_ID, AMOUNT, CURRENCY, STATUS, CREATED_AT)
```

No es necesario disponer de una instancia Oracle para ejecutar la aplicación Java.

## Reporte de pagos con Python

El script:

```text
scripts/payment_report.py
```

consume `GET /payments`, autenticándose previamente mediante `POST /auth/login`.

El reporte agrupa los pagos por cliente y genera:

- monto total
- cantidad de pagos
- monto promedio

Solamente se incluyen pagos con estado `PROCESSED`. Los pagos `FAILED` se excluyen del total, cantidad y promedio.

Los montos se procesan utilizando `Decimal` para evitar problemas de precisión asociados con números de punto flotante.

El archivo generado por defecto es:

```text
reports/payment_summary.csv
```

Para ejecutarlo:

```bash
python3 scripts/payment_report.py
```

También es posible especificar los parámetros:

```bash
python3 scripts/payment_report.py \
  --base-url http://localhost:8080 \
  --email admin@payments.com \
  --password admin123 \
  --output reports/payment_summary.csv
```

El script maneja errores de conexión y autenticación mostrando un mensaje descriptivo y terminando con un código de salida distinto de cero, en lugar de finalizar con una excepción no controlada.

No requiere paquetes externos de Python.

## Tests

Para ejecutar todos los tests:

```bash
./gradlew clean test
```

Se incluyeron pruebas sobre las principales áreas del proyecto, entre ellas:

- transformación de pagos a XML
- escritura del XML
- lógica del servicio de pagos
- validación de los endpoints
- filtros de consulta
- generación y validación de JWT
- autenticación y protección de `/payments`

Las pruebas de seguridad verifican, entre otros escenarios, que `/payments` no pueda accederse sin autenticación y que pueda utilizarse correctamente con un JWT válido.

## Estructura del proyecto

```text
PaymentProcessor/
├── scripts/
│   └── payment_report.py
├── sql/
├── src/
│   ├── main/
│   │   ├── java/com/org/paymentprocessor/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── integration/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   └── test/
├── build.gradle
├── gradlew
└── README.md
```

## Decisiones tomadas

El objetivo fue mantener una solución pequeña pero con separación clara de responsabilidades, priorizando los puntos solicitados en la prueba.

**H2 en memoria.** Evita infraestructura externa y permite levantar el proyecto inmediatamente.

**JWT.** Permite proteger la API manteniendo la autenticación sin estado.

**Persistencia del XML en disco.** Simula la integración con el sistema core de una forma sencilla y verificable.

**DTO separado para XML.** Evita acoplar directamente el modelo interno del servicio al contrato esperado por el sistema core.

**Manejo global de excepciones.** Centraliza las respuestas de error y mantiene una estructura consistente.

**Python sin dependencias externas.** El script utiliza únicamente la librería estándar para que pueda ejecutarse inmediatamente después de clonar el repositorio.

**Tests de seguridad e integración.** Además de las pruebas unitarias mínimas solicitadas, se cubrieron los flujos principales de autenticación y acceso a los endpoints protegidos.

### Índice para la consulta de Top Customers

Se agregó el siguiente índice sobre la tabla `PAYMENTS`:

```sql
CREATE INDEX idx_payments_status_created_customer
    ON payments (status, created_at, customer_id);
```

Este índice busca mejorar la consulta de los 10 clientes con mayor monto pagado, ya que la consulta primero filtra los pagos por status y created_at:

```sql
WHERE p.status = 'PROCESSED'
  AND p.created_at >= SYSDATE - 30
```

Al tener status y created_at al inicio del índice, Oracle puede localizar los pagos procesados dentro del período solicitado sin tener que recorrer necesariamente toda la tabla PAYMENTS.

Se incluye también customer_id, ya que este campo se utiliza posteriormente para relacionar los pagos con los clientes y realizar la agrupación.