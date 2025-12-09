# Manual de Usuario y Guía Técnica Rápida

Versión: 1.0  
Fecha: 2025-12-09

Este documento es un manual de usuario y guía técnica para desarrolladores. Contiene:
- Cómo instalar/configurar y usar la aplicación (usuario/desarrollador).
- Dónde está la arquitectura de paquetes y qué hace cada paquete.
- Dónde y cómo se aplican los principios SOLID (incluido Liskov y Open/Closed).
- Flujo de ejecución (HTTP y sockets) y pautas para extender el sistema.

---

## 1. Resumen general

La aplicación es un motor de procesado de transacciones que acepta peticiones por HTTP (servlet) y por TCP (socket).  
Flujo principal: recibir transacción → validar → procesar (por tipo) → persistir en base de datos.

Componentes principales (visión rápida):
- HTTP endpoint: `/transactions` (POST)
- Socket TCP: puerto por defecto `9000` (una línea JSON por transacción)
- Persistencia: JDBC a tabla `transactions`
- Concurrencia: `ThreadPoolManager` (pool de hilos) y handlers por conexión

---

## 2. Requisitos e instalación rápida

Requisitos
- Java 11+ compatible (o la versión que use tu entorno).
- Contenedor Jakarta Servlet (Tomcat 10+, Payara/GlassFish con Jakarta).
- Driver JDBC para tu BD (MySQL/Postgres) — copiar JAR a `web/WEB-INF/lib/`.
- Base de datos creada con el script `sql/schema.sql`.
- Archivo de configuración `db.properties` en `web/WEB-INF/db.properties` (o `WEB-INF/classes/db.properties`).

Pasos de despliegue
1. Copia `sql/schema.sql` y ejecútalo en tu servidor de BD para crear la base y tabla `transactions`.
2. Edita `web/WEB-INF/db.properties` con tu `jdbc.url`, `jdbc.user`, `jdbc.password` y `jdbc.driver`.
3. Coloca el JAR del driver JDBC en `web/WEB-INF/lib/`.
4. Compila/empaca el WAR (NetBeans / Ant) y despliega en Tomcat 10+.
5. Al iniciar el contenedor, `AppStartupListener` inicializa la aplicación (procesadores, repositorio, thread pool, socket server).

Ejemplo de `db.properties` mínimo:
```properties
jdbc.url=jdbc:mysql://localhost:3306/transactions_db?useSSL=false&serverTimezone=UTC
jdbc.user=tx_user
jdbc.password=tx_password
jdbc.driver=com.mysql.cj.jdbc.Driver
```

Script DDL de ejemplo (resumen de `sql/schema.sql`):
```sql
CREATE DATABASE IF NOT EXISTS transactions_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE transactions_db;

CREATE TABLE IF NOT EXISTS transactions (
  id VARCHAR(64) PRIMARY KEY,
  type VARCHAR(32) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  payload TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 3. Cómo usar la aplicación (operaciones básicas)

A) Vía HTTP (recomendado para front)
- Endpoint: POST /transactions
- Parámetros (form-data):
  - type: PAYMENT o REFUND
  - amount: número decimal (ej. 123.45)
  - payload: texto libre (opcional)

Ejemplo curl:
```bash
curl -X POST -F "type=PAYMENT" -F "amount=100.00" -F "payload=test" http://localhost:8080/<app-context>/transactions
```

Respuesta:
- 200 OK: "Transacción procesada: id=<uuid>"
- 400 Bad Request: errores de validación
- 500 Internal Server Error: errores de procesamiento / BD

B) Vía TCP (socket)
- Puerto por defecto: 9000 (configurable en `AppStartupListener`).
- Protocolo: por conexión, cada línea debe ser un JSON representando la transacción.
- JSON mínimo:
```json
{"type":"PAYMENT","amount":123.45,"payload":"texto opcional","id":"opcional-id"}
```
- Se puede usar `nc localhost 9000` o un cliente TCP. El servidor responde líneas con `OK` o `ERROR ...`.

---

## 4. Administración / Operaciones de mantenimiento

- Logs: revisa los logs del contenedor para mensajes de `AppStartupListener`, errores de inicialización o excepciones de workers.
- Parada limpia: al detener la app el `AppStartupListener.contextDestroyed` cierra el ServerSocket y apaga el `ThreadPoolManager` con `shutdown()` y `awaitTermination`.
- Cambiar puerto socket: modificar la instanciación de `SocketServer` en `web/AppStartupListener.java`.
- Cambiar pool size: modificar `new ThreadPoolManager(<size>)` en `AppStartupListener`.

---

## 5. Estructura de paquetes y responsabilidades (breve)

- model
  - Transaction (interface): id, type, amount, payload.
  - PaymentTransaction, RefundTransaction: implementaciones.

- processor
  - TransactionProcessor (interface): process(tx), supportedType()
  - PaymentProcessor, RefundProcessor: lógica por tipo.
  - ProcessorFactory: registro thread-safe de processors.

- validator
  - TransactionValidator, BasicTransactionValidator: reglas básicas.

- repository
  - TransactionRepository (interface): save(...) [y opcionalmente find/update/delete si se extiende]
  - JdbcTransactionRepository: implementación JDBC.

- service
  - TransactionService: orquesta validación → procesamiento → persistencia.
  - Excepciones: ValidationException, ProcessingException, RepositoryException.

- web
  - TransactionServlet: endpoint HTTP `/transactions`.
  - AppStartupListener: inicialización global (registros, repo, service, pool, socket server).

- concurrent
  - ThreadPoolManager: wrapper de ExecutorService.
  - TransactionWorker, WorkQueue: consumidores opcionales.

- network
  - SocketServer, SocketClientHandler, SocketProtocol: servidor TCP y handlers.

- util
  - IdGenerator, JsonUtils (Gson-based).

---

## 6. Dónde y cómo se aplican los principios SOLID (explicación breve y directa)

S — Single Responsibility Principle (Responsabilidad única)
- Cada clase hace una sola cosa:
  - `TransactionService` → orquestación del flujo.
  - `JdbcTransactionRepository` → acceso/persistencia a BD.
  - `PaymentProcessor` → lógica específica del pago.
  - `ThreadPoolManager` → gestiona el pool de hilos.
- Resultado: cambios locales, menos efectos colaterales.

O — Open/Closed Principle (Abierto para extensión, cerrado para modificación)
- `ProcessorFactory` y la arquitectura de `TransactionProcessor` permiten añadir nuevos tipos de transacción sin modificar `TransactionService` ni el core.
  - Pasos para extender: crear nueva clase que implemente `TransactionProcessor` y registrarla en `ProcessorFactory` (idealmente desde `AppStartupListener`).

L — Liskov Substitution Principle (Sustitución de Liskov)
- Las interfaces (por ejemplo `Transaction`, `TransactionProcessor`, `TransactionRepository`) permiten sustituir implementaciones concretas sin romper el cliente:
  - `TransactionService` usa `TransactionProcessor` sin asumir la implementación concreta; cualquier `TransactionProcessor` válido funciona.
  - Si se sustituye `JdbcTransactionRepository` por `JdbcTransactionRepositoryJndi` o por un mock para pruebas, `TransactionService` sigue funcionando.

I — Interface Segregation Principle (Segregación de interfaces)
- Interfaces pequeñas y enfocadas:
  - `TransactionRepository` expone solo lo necesario (save) y no fuerza métodos adicionales.
  - `TransactionProcessor` expone `process` y `supportedType` únicamente.

D — Dependency Inversion Principle (Inversión de dependencias)
- `TransactionService` depende de abstracciones:
  - Constructor recibe `TransactionValidator` y `TransactionRepository` (interfaces), no implementaciones concretas.
  - Esto facilita tests unitarios y cambiar la persistencia (DriverManager → DataSource) sin cambiar la lógica de negocio.

---

## 7. Flujo de ejecución detallado (paso a paso)

A) Flujo HTTP (POST /transactions)
1. Cliente envía POST a `/transactions` con `type`, `amount`, `payload`.
2. `TransactionServlet` crea la instancia concreta (`PaymentTransaction` o `RefundTransaction`) y obtiene `TransactionService` desde `ServletContext`.
3. `TransactionService.handle(tx)`:
   a. Llama a `validator.validate(tx)` (BasicTransactionValidator).
   b. Obtiene `TransactionProcessor` desde `ProcessorFactory.getProcessor(tx.getType())`.
   c. Llama a `processor.process(tx)` (ej. PaymentProcessor).
   d. Llama a `repository.save(tx)` (JdbcTransactionRepository).
4. `JdbcTransactionRepository.save` abre una `Connection` (DriverManager/DataSource) y ejecuta INSERT.
5. Respuesta al cliente: éxito o error.

B) Flujo TCP (Socket)
1. `SocketServer` acepta la conexión y delega a `SocketClientHandler`, ejecutado por `ThreadPoolManager`.
2. `SocketClientHandler` lee líneas; cada línea la parsea con `JsonUtils.parseTransaction(json)`.
3. Por cada `Transaction` parseada:
   - Llamada a `txService.handle(tx)` (igual flujo que HTTP) — puede ejecutarse de forma sincrónica o en otro runnable dependiendo de la configuración.
4. Respuesta por socket: `OK id=...` o `ERROR ...`.

Notas de concurrencia
- No compartir objetos `Connection` entre hilos.
- `ProcessorFactory` usa `ConcurrentHashMap` para registrar processors thread-safe.
- `ThreadPoolManager` controla el número de hilos y evita crear hilos ilimitados.

---

## 8. Cómo extender el sistema (pautas rápidas)

Añadir un nuevo tipo de transacción:
1. Crear clase en `model` si requiere datos específicos (opcional).
2. Implementar `TransactionProcessor` y su método `process`.
3. Registrar el processor: `ProcessorFactory.register(new MyNewProcessor())` (hacerlo en `AppStartupListener`).
4. (Opcional) Añadir pruebas unitarias para el nuevo processor.

Cambiar la persistencia a DataSource/JNDI:
1. Crear `JdbcTransactionRepositoryJndi` que busque `DataSource` por JNDI.
2. Configurar `context.xml` / recurso en Tomcat.
3. Actualizar `AppStartupListener` para instanciar la implementación JNDI en vez de la que usa `db.properties`.

Añadir CRUD (read/update/delete):
- Extender `TransactionRepository` con `findById`, `findAll`, `update`, `deleteById`.
- Implementar en `JdbcTransactionRepository`.
- Añadir endpoints (servlets) para GET/PUT/DELETE y vistas JSP/JS en front.

---

## 9. Errores frecuentes y cómo resolverlos (breve)

- "No se encontró db.properties"
  - Verifica que `web/WEB-INF/db.properties` o `WEB-INF/classes/db.properties` exista y que `AppStartupListener` lo lea.
- "Driver not found"
  - Asegúrate de que el JAR del driver está en `web/WEB-INF/lib/` o en el classpath del servidor.
- "Access denied" / login failed
  - Usuario/contraseña incorrectos o permisos DB insuficientes. Revisar GRANTs en la BD.
- "Puerto 9000 ocupado"
  - Cambiar puerto en `AppStartupListener` o liberar puerto.
- "Concurrent modification / lock"
  - Revisa gestión de transacciones; usar control de versiones o transacciones SQL para operaciones compuestas.

---

## 10. Preguntas frecuentes (rápido)

P: ¿Puedo enviar JSON por HTTP en vez de form-data?  
R: Actualmente `TransactionServlet` espera form-data. Podemos adaptarlo a `application/json` si se coordina (añadir parseo con Gson/Jackson).

P: ¿Dónde se añade un nuevo processor?  
R: `processor/` — crear la clase y registrar en `ProcessorFactory` desde `AppStartupListener`.

P: ¿Cómo pruebo sin front?  
R: Usa curl para HTTP y `nc` para sockets.

---

## 11. Anexos (snippets útiles)

A) Ejemplo de curl:
```bash
curl -X POST -F "type=PAYMENT" -F "amount=50.00" -F "payload=test" http://localhost:8080/miApp/transactions
```

B) Ejemplo JSON por socket (en `nc`):
```text
{"type":"PAYMENT","amount":45.5,"payload":"compra 123"}
```

C) Clase de prueba de conexión (DbTest.java):
```java
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DbTest {
  public static void main(String[] args) throws Exception {
    try (InputStream in = DbTest.class.getClassLoader().getResourceAsStream("db.properties")) {
      if (in == null) { System.err.println("No se encontró db.properties"); return; }
      Properties p = new Properties();
      p.load(in);
      String url = p.getProperty("jdbc.url");
      String user = p.getProperty("jdbc.user");
      String pass = p.getProperty("jdbc.password");
      String driver = p.getProperty("jdbc.driver");
      if (driver != null && !driver.isEmpty()) Class.forName(driver);
      try (Connection c = DriverManager.getConnection(url, user, pass)) {
        System.out.println("Conectado OK a: " + url);
      }
    }
  }
}
```

---

## 12. Conclusión

La aplicación está diseñada para ser:
- Sencilla de entender y consumir por el front (POST /transactions).
- Extensible para nuevos tipos de transacción (Open/Closed).
- Segura en cuanto a concurrencia básica (cada hilo abre su conexión).
- Flexible para cambiar persistencia por JNDI/DataSource.

Si necesitas que genere:
- Un PDF/README listo para mandar por correo,
- Un ejemplo de servlet adicional (GET/PUT/DELETE),
- O un ZIP con plantillas JSP + db.properties configurado,

dime cuál prefieres y lo preparo.
```
