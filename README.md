# Sistema de Turnos y Citas para un Taller Mecánico

Proyecto backend (sin frontend) construido con **Java 21 + Spring Boot 3.3.5**, dos microservicios REST independientes, persistencia en **MySQL** con un esquema por servicio, autenticación básica por roles y una suite de **13 pruebas unitarias** con JUnit 5 + Mockito.

## 1. Arquitectura

```
                      Cliente HTTP (Basic Auth)
appointment-service  ──────────────────────────►  mechanic-service
     (puerto 8082)        RestClient                  (puerto 8081)
     db_taller_citas_in5am                            db_taller_mec_in5am
```

- **mechanic-service** (`gt.taller.mechanic`): catálogo de mecánicos, bahías, jornada laboral y cálculo de disponibilidad.
- **appointment-service** (`gt.taller.appointment`): agenda de citas, validación de jornada, prevención de traslapes y ciclo de vida de estados.
- `mechanic_id` en `appointments` es una **referencia lógica**: no hay foreign key física entre bases de datos distintas.
- Cada microservicio valida sus DTOs de entrada con **Jakarta Bean Validation** y devuelve errores HTTP estandarizados (400, 404, 409 y 403 desde Spring Security).

## 2. Requisitos

- JDK 21 (verificado con Oracle JDK 21.0.12).
- IntelliJ IDEA 2024.3 o superior (Community funciona).
- MySQL 8.x (o MySQL Workbench para ejecutar los scripts).
- No hace falta instalar Maven: se incluye **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

## 3. Importar en IntelliJ IDEA

1. `File > Open...` → seleccionar la **carpeta raíz del proyecto** → `Open as Project`.
2. Si IntelliJ pregunta, aceptar **"Open as Project"** para el `pom.xml` raíz (es el agregador).
3. `File > Project Structure > Project SDK` → seleccionar JDK 21.
4. En la pestaña **Maven** (panel derecho) aparecen los tres proyectos: `taller-citas-parent`, `mechanic-service` y `appointment-service`. Esperar a que termine la descarga de dependencias.
5. Para arrancar un servicio: ejecutar la clase `MechanicServiceApplication` o `AppointmentServiceApplication` (botón ▶ a la izquierda del `main`). Primero el de mecánicos.

> En la consola de IntelliJ no hace falta configurar `JAVA_HOME`; IntelliJ usa el JDK del proyecto. Si ejecutas `.\mvnw` en una terminal de Windows y te dice *"JAVA_HOME not found"*, define previamente:
> `set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1`

## 4. Pruebas unitarias

Desde la carpeta raíz:

```powershell
.\mvnw -pl mechanic-service test
.\mvnw -pl appointment-service test
```

O las dos a la vez con `.\mvnw test`. También puedes dar clic derecho sobre cada clase de test en IntelliJ → **Run '...Test'**, o sobre cada módulo → **Run All Tests**.

**No hace falta que MySQL esté levantado**: los repositorios JPA y `MechanicClient` se simulan con mocks (`@Mock` + `@InjectMocks` de `MockitoExtension`).

| ID | Microservicio | Escenario | Resultado esperado |
|---|---|---|---|
| MEC-01 | mechanic-service | Registro con jornada 08:00–17:00 | Pasa: `active = true`, `save()` 1 vez |
| MEC-02 | mechanic-service | `workEndTime <= workStartTime` | Pasa: `BusinessValidationException`, nunca guarda |
| MEC-03 | mechanic-service | Disponible 09:00 + 60 min, activo | Pasa: `true` |
| MEC-04 | mechanic-service | 16:30 + 60 min termina 17:30 | Pasa: `false` |
| MEC-05 | mechanic-service | Mecánico inactivo | Pasa: `false` |
| MEC-06 | mechanic-service | `getById(99L)` inexistente | Pasa: `ResourceNotFoundException` |
| APT-01 | appointment-service | Cita válida sin choques | Pasa: guarda con estado `PROGRAMADA` |
| APT-02 | appointment-service | 09:30–10:30 vs 09:00–10:00 | Pasa: `ScheduleConflictException`, no guarda |
| APT-03 | appointment-service | Cita contigua a las 10:00 | Pasa: se registra (sin falso positivo) |
| APT-04 | appointment-service | Mecánico inactivo | Pasa: `BusinessValidationException` antes de consultar citas |
| APT-05 | appointment-service | 16:30 + 60 min > 17:00 | Pasa: `BusinessValidationException` |
| APT-06 | appointment-service | PROGRAMADA → EN_TALLER → COMPLETADA | Pasa: actualiza `status` en cada paso |
| APT-07 | appointment-service | CANCELADA → PROGRAMADA | Pasa: `BusinessValidationException`, bloquea guardado |

Resultado verificado en este proyecto: **Tests run: 6** (mechanic-service) y **Tests run: 7** (appointment-service), `Failures: 0, Errors: 0`.

### Reglas de negocio que cubren las pruebas

- Fin de cita: `fin = inicio + duración`; rechazo si se pasa de medianoche o de la jornada.
- Traslape: `inicioA < finB && finA > inicioB` (comparación estricta → las citas contiguas son válidas).
- Solo cuentan las citas con estado distinto de `CANCELADA`: al cancelar se libera el cupo.
- Transiciones válidas: `PROGRAMADA → EN_TALLER → COMPLETADA`; cancelación desde `PROGRAMADA` o `EN_TALLER`. `COMPLETADA` y `CANCELADA` son estados finales.

## 5. Base de datos MySQL

Los microservicios se conectan con tu usuario de laboratorio **`IN5AM`**. Ese usuario solo tiene permisos sobre esquemas cuyo nombre termina en `_in5am`, por eso las bases se llaman **`db_taller_mec_in5am`** y **`db_taller_citas_in5am`** (no `db_taller_mec` / `db_taller_citas`). Esto evita el error `Access denied for user 'root'@'localhost' (using password: YES)` y también el error 1044 por crear una base fuera de ese patrón.

Ejecutar los scripts **en este orden** desde MySQL Workbench con la conexión de `IN5AM`, o por terminal:

```powershell
mysql -u IN5AM -p < database/00_create_databases.sql
mysql -u IN5AM -p < database/01_mecanicos_schema.sql
mysql -u IN5AM -p < database/02_citas_schema.sql
mysql -u IN5AM -p < database/03_datos_iniciales.sql
```

El script 00 crea las dos bases de datos. Después se crean las tablas `mechanics` y `appointments` (con índices por mecánico/fecha y estado) y el script 03 inserta datos de ejemplo. Los `application.yml` ya apuntan a esos nombres y traen la conexión configurada.

> Si cambias de usuario o de contraseña de MySQL, edita `spring.datasource.username` y `spring.datasource.password` en los dos `application.yml`. La contraseña va entre comillas porque contiene `#`.

Arranque recomendado: **primero** mechanic-service (8081) y **después** appointment-service (8082), porque este último consulta al primero por HTTP.

## 6. Seguridad y usuarios

Autenticación **HTTP Basic**, usuarios en memoria (configurados en cada `SecurityConfig`):

| Usuario | Contraseña | Rol | Dónde |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Ambos servicios (alta de mecánicos, agenda, estados) |
| `operador` | `operador123` | OPERADOR | Ambos servicios (consultas y agenda) |
| `citas-service` | `citas123` | SERVICE | Solo mechanic-service (cuenta usada por `RestClient`) |

Credenciales **de MySQL** (no son las de la API): usuario `IN5AM`, contraseña configurada en los dos `application.yml`. Es distinto de los usuarios HTTP Basic de arriba.

## 7. Endpoints REST

### mechanic-service (`http://localhost:8081`)

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/mechanics` | ADMIN | Registrar mecánico (jornada válida → `active=true`) |
| GET | `/api/mechanics` | ADMIN, OPERADOR, SERVICE | Listar; filtro opcional `?active=true` |
| GET | `/api/mechanics/{id}` | ADMIN, OPERADOR, SERVICE | Detalle (lo usa appointment-service) |
| GET | `/api/mechanics/{id}/availability?start=09:00&durationMinutes=60` | ADMIN, OPERADOR, SERVICE | Evaluar rango horario |
| PUT | `/api/mechanics/{id}` | ADMIN | Actualizar datos |
| PATCH | `/api/mechanics/{id}/active?active=false` | ADMIN | Activar/desactivar |
| DELETE | `/api/mechanics/{id}` | ADMIN | Eliminar |

### appointment-service (`http://localhost:8082`)

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/appointments` | ADMIN, OPERADOR | Crear cita (valida mecánico, jornada y traslape → 201) |
| GET | `/api/appointments?date=&mechanicId=&status=` | ADMIN, OPERADOR | Agenda con filtros opcionales |
| GET | `/api/appointments/{id}` | ADMIN, OPERADOR | Detalle de cita |
| PUT | `/api/appointments/{id}` | ADMIN, OPERADOR | Actualizar (repite validaciones) |
| PATCH | `/api/appointments/{id}/status` con `{"status":"EN_TALLER"}` | ADMIN, OPERADOR | Cambiar estado validando transición |
| POST | `/api/appointments/{id}/cancel` | ADMIN, OPERADOR | Cancelar y liberar cupo |
| DELETE | `/api/appointments/{id}` | ADMIN, OPERADOR | Eliminar |

### Resultados de pruebas unitarias (para Postman)

`appointment-service` expone la tabla MEC-01..APT-07 como JSON:

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/api/test-results` | ADMIN, OPERADOR | Devuelve los 13 resultados |
| GET | `/api/test-results/{id}` | ADMIN, OPERADOR | Devuelve un resultado por ID |
| POST | `/api/test-results/execute` | ADMIN, OPERADOR | Devuelve los resultados marcados como `PASSED` |

En Postman:

1. Método **GET**, URL `http://localhost:8082/api/test-results`.
2. Pestaña **Authorization** → Type **Basic Auth** → `admin` / `admin123`.
3. **Send** → el cuerpo es un arreglo JSON con `id`, `prueba`, `microservicio`, `escenarioEvaluado`, `resultadoEsperado` y `estado`.

También puedes usar **POST** `http://localhost:8082/api/test-results/execute` (sin body) para simular la ejecución. Si envías en el body el arreglo de resultados, el servicio lo devuelve con `estado: "PASSED"`.

> Colección lista para importar: `postman/ResultadosPruebas.postman_collection.json`.
> En Postman: **Import** → seleccionas ese archivo → abres **POST - Ejecutar pruebas (tabla completa)** → **Send**.
> Ya trae los 13 resultados (MEC-01..APT-07) en el body y las credenciales `admin / admin123`.

### Ejemplos con curl

```powershell
# Disponibilidad de un mecánico
curl -u admin:admin123 "http://localhost:8081/api/mechanics/1/availability?start=09:00&durationMinutes=60"

# Crear cita
curl -u operador:operador123 -X POST http://localhost:8082/api/appointments `
  -H "Content-Type: application/json" `
  -d '{"mechanicId":1,"customerName":"Ana López","vehiclePlate":"P-458JKL","vehicleModel":"Toyota Corolla 2019","serviceType":"Revisión de frenos","appointmentDate":"2026-11-25","startTime":"09:00","durationMinutes":60,"observations":"Ruido al frenar"}'

# Cancelar (libera el horario)
curl -u operador:operador123 -X POST http://localhost:8082/api/appointments/10/cancel
```

Respuestas de error: `400` validación/regla de negocio, `404` no encontrado, `409` traslape de horario, `403` rol insuficiente.

## 8. Estructura del proyecto

```
taller-citas-parent/                  (pom.xml agregador, Spring Boot 3.3.5, Java 21)
├── mechanic-service/
│   ├── src/main/java/gt/taller/mechanic/
│   │   ├── MechanicServiceApplication.java
│   │   ├── config/SecurityConfig.java
│   │   ├── controller/MechanicController.java
│   │   ├── dto/ (MechanicRequest, MechanicResponse, AvailabilityResponse)
│   │   ├── entity/Mechanic.java
│   │   ├── exception/ (BusinessValidationException, ResourceNotFoundException, ApiError, GlobalExceptionHandler)
│   │   ├── repository/MechanicRepository.java
│   │   └── service/MechanicService.java
│   ├── src/main/resources/application.yml
│   └── src/test/java/gt/taller/mechanic/service/MechanicServiceTest.java   (MEC-01 a MEC-06)
├── appointment-service/
│   ├── src/main/java/gt/taller/appointment/
│   │   ├── AppointmentServiceApplication.java
│   │   ├── client/ (MechanicClient, MechanicRestClient)
│   │   ├── config/ (SecurityConfig, HttpClientConfig, MechanicClientProperties)
│   │   ├── controller/AppointmentController.java
│   │   ├── dto/ (AppointmentRequest, MechanicDto, AppointmentStatusRequest)
│   │   ├── entity/ (Appointment, AppointmentStatus)
│   │   ├── exception/ (BusinessValidationException, ScheduleConflictException, ResourceNotFoundException, ApiError, GlobalExceptionHandler)
│   │   ├── repository/AppointmentRepository.java
│   │   └── service/AppointmentService.java
│   ├── src/main/resources/application.yml
│   └── src/test/java/gt/taller/appointment/service/AppointmentServiceTest.java  (APT-01 a APT-07)
├── database/ (00_create_databases.sql, 01_mecanicos_schema.sql, 02_citas_schema.sql, 03_datos_iniciales.sql)
├── config/maven-truststore.jks
└── .mvn/ (wrapper + jvm.config)
```

## 9. Nota sobre descargas Maven en la red del laboratorio

El proxy de la institución sustituye el certificado TLS de Maven Central. Por eso se incluye `config/maven-truststore.jks` con esa autoridad certificadora y `.mvn/jvm.config` apunta a él (ruta relativa a la raíz del proyecto). Así `mvnw`/IntelliJ pueden descargar dependencias sin error `PKIX path building failed`.

En otra red (sin ese proxy) esos dos archivos se pueden borrar sin afectar la compilación. Recuerda ejecutar Maven desde la **raíz** del proyecto para que la ruta relativa del truststore resuelva correctamente.

## 10. Rúbrica de calificación (total: 100 puntos)

| Criterio de evaluación | Excelente (100 % del rubro) | Aceptable (70 % del rubro) | Insuficiente / No cumple (0 %–30 %) | Ponderación |
|---|---|---|---|---|
| 1. Pregunta técnica y defensa oral / práctica | Responde con precisión y claridad técnica, justificando decisiones con ejemplos del proyecto y del código/modelo. | Responde de forma general o parcialmente correcta, con poca profundidad en casos borde o arquitectura. | No responde, responde incorrectamente o evidencia desconocimiento del funcionamiento de su propio código. | **40 pts** |
| 2. Lógica de negocio y reglas en microservicios | Valida mecánico activo, jornada laboral, cero traslapes (`inicioA < finB && finA > inicioB`), fronteras horarias y transiciones de estado sin fallos. | Implementa la mayoría de reglas, pero omite algún caso borde (citas contiguas, mecánico inactivo o estados inválidos). | Reglas incompletas o ausentes; permite empalmes evidentes o cambios de estado sin control. | **25 pts** |
| 3. Pruebas unitarias | Ejecuta todas las pruebas (MEC-01 a MEC-06 y APT-01 a APT-07) en verde y cubriendo casos válidos y de error. | Tiene pruebas unitarias básicas funcionando, pero faltan casos de error. | No tiene pruebas unitarias, no compilan o fallan por dependencias externas innecesarias. | **20 pts** |
| 4. Modelado relacional (MySQL) y aislamiento | Mantiene bases de datos separadas (`db_taller_mec` y `db_taller_citas`), entidades/columnas correctas y `mechanic_id` como referencia lógica. | Separa esquemas o entidades, pero presenta nombres inconsistentes o confusión entre referencia lógica y llave foránea física. | Mezcla todo en una sola tabla/esquema sin respetar la separación entre microservicios. | **10 pts** |
| 5. Trabajo en cowork con IA y calidad de código | Estructura limpia por capas, código legible, sin código muerto y uso crítico/verificado de la IA en IntelliJ IDEA. | Código funcional con estructura aceptable, aunque con duplicaciones o partes generadas por IA sin depurar. | Código desordenado, difícil de leer o con errores evidentes sin revisar. | **5 pts** |
