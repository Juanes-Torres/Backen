# KAIRÓS backend: 12 factores y principios SOLID

Este documento explica cómo el backend aplica la metodología de **12 factores** (aplicaciones listas para desplegar en la nube) y los principios **SOLID** (código fácil de cambiar y de probar). Para cada punto se indica **dónde se ve en el código**.

> Regla del equipo: todo cambio nuevo debe respetar estos puntos. La prueba `ArquitecturaTest` revisa automáticamente las reglas de las capas en cada `mvnw test`.

---

## Parte 1. Los 12 factores

| # | Factor | Cómo lo cumple KAIRÓS | Dónde verlo |
|---|---|---|---|
| I | **Código base**: un repositorio por aplicación, muchos despliegues | Un repositorio para el backend (el frontend tiene el suyo). Las ramas `desarrollo`, `pre-produccion` y `produccion` son los despliegues del mismo código | GitHub `Juanes-Torres/Backen` |
| II | **Dependencias** declaradas y aisladas | Todas las librerías están en `pom.xml` con versión. El Maven Wrapper (`mvnw`) descarga la versión exacta de Maven; no hace falta tener nada instalado | `pom.xml`, `mvnw.cmd` |
| III | **Configuración** en el entorno | Todo lo que cambia entre ambientes sale de variables de entorno (`DB_URL`, `JWT_SECRET`, `MAIL_PASSWORD`...). En pre-producción y producción no hay valores por defecto: si falta una variable, la app no arranca. Los secretos nunca se suben a Git | `application*.properties`, `.env.example`, `secrets.properties.example`, `scripts/ambiente.ps1` |
| IV | **Servicios de respaldo** como recursos conectables | PostgreSQL y el correo (SMTP de Gmail) se conectan por URL y credenciales. Cambiar de base de datos o de proveedor de correo es cambiar variables, no código. El correo está detrás de `NotificacionPort` | `NotificacionPort`, `GmailNotificacionAdapter` |
| V | **Construir, publicar, ejecutar** separados | *Construir*: el `Dockerfile` compila y arma el `.jar`. *Publicar*: esa imagen más las variables del ambiente. *Ejecutar*: `docker compose up` o `java -jar`. El mismo `.jar` sirve para todos los ambientes | `Dockerfile`, `docker-compose.yml` |
| VI | **Procesos sin estado** | No hay sesión en memoria: cada petición trae su token JWT (`SessionCreationPolicy.STATELESS`). Todo lo que debe durar se guarda en PostgreSQL | `SecurityConfig`, `JwtAuthenticationFilter` |
| VII | **Asignación de puerto** | La app trae su propio servidor (Tomcat embebido) y escucha en `SERVER_PORT` (8080 por defecto) | `application.properties` |
| VIII | **Concurrencia**: escalar con más procesos | Como no guarda estado, se pueden levantar varias copias del backend detrás de un balanceador. Las tareas lentas (correo) van en segundo plano con `@Async` | `AsyncConfig`, `GmailNotificacionAdapter` |
| IX | **Desechabilidad**: arranque rápido y apagado ordenado | Arranca en unos 6 segundos. Al apagarse termina las peticiones en curso antes de cerrar (`server.shutdown=graceful`, máximo 20 s) | `application.properties` |
| X | **Paridad entre desarrollo y producción** | Se usa PostgreSQL 18 en todos los ambientes (también en Docker), las mismas migraciones de Flyway y el mismo `.jar` | `docker-compose.yml`, `db/migration` |
| XI | **Registros (logs) como flujo de eventos** | La app escribe sus logs en la salida estándar (consola) y no maneja archivos de log; quien la ejecuta (Docker, el servidor) decide dónde guardarlos | Logs de SLF4J en los servicios y adaptadores |
| XII | **Procesos de administración** como tareas únicas | Las migraciones de Flyway (`V1`…`V4`) se aplican solas al arrancar, una sola vez cada una. El administrador y el superadministrador iniciales se crean al arrancar solo si no existen | `db/migration`, `AdminInicial`, `SuperadminInicial` |

### Ajustes hechos para cumplir mejor los 12 factores

- **III**: `jwt.expiration-ms`, `spring.mail.host`, `spring.mail.port` y `kairos.frontend.url` estaban escritos fijos en los archivos. Ahora salen de `JWT_EXPIRATION_MS`, `MAIL_HOST`, `MAIL_PORT` y `FRONTEND_URL`, con los mismos valores por defecto.
- **IX**: se agregó el apagado ordenado (`server.shutdown=graceful`).

### Pendientes conocidos (para tener en cuenta)

- **VI, en ventas a crédito (Sprint 7)**: las fotos del contrato se planearon en una carpeta del servidor. Si se levantan varias copias del backend, cada una tendría sus propias fotos. Para cumplir el factor VI hay que guardarlas en un volumen compartido de Docker o en un almacenamiento externo.
- **IX y X**: no hay un endpoint de salud (`/actuator/health`) para que Docker sepa si el backend está listo. Requiere agregar la dependencia `spring-boot-starter-actuator` (pendiente de aprobación).

---

## Parte 2. Principios SOLID

La arquitectura hexagonal ya obliga a seguir varios de estos principios. Este es el detalle:

### S — Responsabilidad única

*Cada clase tiene una sola razón para cambiar.*

- **Controllers** (`adapter/in/web`): solo traducen HTTP/JSON a casos de uso. No tienen reglas de negocio.
- **Servicios** (`application/usecase`): un servicio por grupo de casos de uso (`RegistrarEmpresaService`, `AutenticarUsuarioService`, `InventarioService`...).
- **Dominio** (`domain/model`): las reglas de cada entidad. Por ejemplo, `Inventario` impide el stock negativo y `Empresa` decide qué cambios de estado se permiten.
- **Adaptadores de persistencia**: solo guardan y buscan; los *mappers* solo convierten entre entidad JPA y modelo del dominio.
- **Ejemplo de ajuste:** la regla de *quién puede ver o modificar a quién* estaba dentro de `BuscarUsuarioService` y otro servicio la tomaba prestada. Ahora vive sola en `PermisosUsuario`.

### O — Abierto/cerrado

*Se puede extender sin modificar lo que ya funciona.*

- **Notificaciones:** `NotificacionPort` tiene dos adaptadores (`GmailNotificacionAdapter` y `NotificacionDeshabilitadaAdapter`) que se eligen por configuración. Para avisar por SMS o WhatsApp se agrega otro adaptador; `RegistrarUsuarioService` no cambia.
- **Estados de empresa:** `EmpresaService` aplica cualquier cambio de estado con el mismo método (`cambiarEstado(id, Empresa::aprobar)`). Agregar un estado nuevo es agregar un método en `Empresa`, sin tocar el flujo.

### L — Sustitución de Liskov

*Cualquier implementación de una interfaz se puede usar en lugar de otra sin romper nada.*

- Las pruebas de los servicios reemplazan los adaptadores reales (PostgreSQL, Gmail, JWT) por **clases falsas escritas a mano** que cumplen el mismo contrato, y los servicios funcionan igual. Ver `RegistrarUsuarioServiceTest`, `AutenticarUsuarioServiceTest` y `ProductoServiceTest`.
- Los contratos están escritos en cada puerto. Por ejemplo, `ProductoRepositoryPort.buscarEnCatalogo` dice que solo devuelve productos de empresas activas, y la clase falsa de la prueba respeta esa regla.

### I — Segregación de interfaces

*Nadie debe depender de métodos que no usa.*

- **Puertos pequeños y por tema:** `RegistrarUsuarioUseCase`, `BuscarUsuarioUseCase` y `ActualizarUsuarioUseCase` en vez de un solo "UsuarioService" gigante. Hay un puerto de salida por entidad.
- **`ConsultarEmpresasActivasUseCase`** (público) va aparte de **`GestionarEmpresasUseCase`** (superadmin): la consulta pública no ve las acciones de aprobar o suspender.
- **Ejemplo de ajuste:** `registrarSuperadmin` estaba en `RegistrarUsuarioUseCase`, así que `UsuarioController` lo veía aunque no debía usarlo. Ahora está en su propia interfaz, `CrearSuperadminUseCase`, que solo usa el arranque (`SuperadminInicial`).
- **Limpieza:** se quitaron métodos de los puertos que ya nadie usaba (`UsuarioRepositoryPort.listarTodos` y `ProductoRepositoryPort.existePorId`).

### D — Inversión de dependencias

*Las reglas de negocio no dependen de los detalles técnicos; los detalles dependen de las reglas.*

- Los servicios dependen de **interfaces** (`UsuarioRepositoryPort`, `TokenPort`, `PasswordEncoderPort`, `NotificacionPort`), no de JPA, JWT, BCrypt ni Gmail. Spring inyecta el adaptador real.
- Las dependencias van hacia adentro: `infrastructure → application → domain`.
- **`ArquitecturaTest`** lo vigila en cada `mvnw test`:
  - el dominio no importa Spring, JPA, la aplicación ni la infraestructura;
  - la aplicación no importa la infraestructura, ni JPA, ni la web;
  - los controllers no importan servicios ni la persistencia;
  - la persistencia no importa la web.

  Si alguien rompe una regla, la prueba falla y muestra el archivo y el `import` culpables.

---

## Cómo explicarlo en la review (resumen)

1. **El flujo de una petición respeta SOLID:** `Controller → UseCase (interfaz) → Service → Dominio → RepositoryPort (interfaz) → PersistenceAdapter → PostgreSQL`.
2. **Se puede probar sin base de datos ni Gmail** gracias a la inversión de dependencias y a Liskov (clases falsas).
3. **Se despliega en cualquier ambiente sin cambiar código** gracias a la configuración por variables de entorno, Docker y Flyway (factores III, V, X y XII).
4. **Hay una prueba automática que impide romper la arquitectura** (`ArquitecturaTest`).
