# CLAUDE.md — Backend KAIRÓS

Este archivo le da a Claude Code todo el contexto del proyecto. Léelo completo antes de hacer cualquier cambio.

## 1. Con quién trabajas

- El usuario es **Juan Esteban Torres**, estudiante de Ingeniería de Sistemas. **Es su primer proyecto de software (nivel junior).**
- **Responde siempre en español**, con explicaciones simples: qué vas a hacer, por qué y qué debería ver el usuario.
- Trabaja **paso a paso**. Después de cada paso importante, di en 2–3 líneas qué hiciste y qué sigue.
- **Pide confirmación antes de:** borrar archivos o carpetas, tocar la base de datos o cualquier acción difícil de deshacer.
- **Tú te encargas de Git y GitHub** (commits, ramas, push y Pull Requests) siguiendo la sección 7. El usuario te lo pidió expresamente. Solo avísale qué subiste.
- Si algo falla, muestra el error, explícalo en palabras simples y propón la solución.

## 2. El proyecto

KAIRÓS es un sistema web de inventario y ventas para franquicias de tecnología (proyecto integrador, Universidad de Cundinamarca, Ingeniería de Software I). Equipo: Daniel Galeano y Juan Esteban Torres.

| Elemento | Valor |
|---|---|
| Ruta del backend | `C:\Users\JUANESTC\Downloads\Kairos-backend\Kairos-backend` (carpeta con `pom.xml`) |
| Paquete base | `com.kairos.Kairos_backend` |
| Stack | Java 21 (se ejecuta con JDK 23), Spring Boot 4.1.1, Spring Security 7, JJWT 0.13.0, Spring Data JPA, Flyway, PostgreSQL 18, Maven Wrapper |
| Base de datos | `jdbc:postgresql://localhost:5432/kairos_db`, usuario `kairos`, contraseña `kairos123` |
| Puerto | 8080 |
| Pruebas de API | Postman |
| Frontend | React + TypeScript (Vite) en un proyecto **aparte**. **No es parte de esta tarea.** |

## 3. Reglas de arquitectura (obligatorias)

El proyecto usa **arquitectura hexagonal**. El docente la evalúa, así que no se puede romper.

```
com.kairos.Kairos_backend
├── domain                 Java PURO: modelos, reglas de negocio, excepciones
│   ├── model
│   └── exception
├── application
│   ├── port/in            interfaces de casos de uso (XxxUseCase)
│   ├── port/out           interfaces hacia afuera (XxxRepositoryPort, TokenPort, PasswordEncoderPort)
│   └── usecase            implementaciones (XxxService, @Service)
└── infrastructure
    ├── adapter/in/web     controllers REST, DTOs (records), GlobalExceptionHandler
    ├── adapter/out/persistence   entity (@Entity), repository (JpaRepository), mapper, XxxPersistenceAdapter
    ├── security           JWT, filtro, BCrypt, handlers 401/403
    └── config             SecurityConfig, AdminInicial
```

- **`domain` no puede importar nada de Spring, JPA (`jakarta.persistence`) ni HTTP.**
- Las dependencias van hacia adentro: `infrastructure → application → domain`.
- Los controllers solo conocen los puertos de entrada (`XxxUseCase`), nunca los Services ni los JpaRepository.
- Las reglas de negocio viven en el dominio o en los Services, **nunca en los controllers**.
- Las respuestas de la API usan DTOs. **Nunca se devuelve `passwordHash`.**
- Excepciones del dominio y su código HTTP: `ReglaNegocioException` → 400, `CredencialesInvalidasException` → 401, `RecursoNoEncontradoException` → 404, `RecursoDuplicadoException` → 409.

## 4. Reglas críticas

1. **NUNCA modificar `src/main/resources/db/migration/V1__nucleo.sql`.** Ya fue aplicada: si cambia, Flyway falla con *checksum mismatch*. Los cambios nuevos van en `V2__…`, `V3__…`.
2. `spring.jpa.hibernate.ddl-auto=validate`: las tablas las crea **solo Flyway**, nunca Hibernate.
3. **No subir secretos a Git.** Los valores de `application.properties` son solo para desarrollo local (`${JWT_SECRET:...}`, `${DB_PASSWORD:...}`).
4. **No agregar dependencias al `pom.xml`** sin explicar por qué y sin la aprobación del usuario.
5. Si una prueba falla, arregla el código, no la prueba (salvo que la prueba esté mal y lo expliques).

## 5. Estado actual (lo que ya funciona)

- Conexión a PostgreSQL y Flyway con `V1__nucleo.sql` (tablas: almacen, usuario, categoria, producto, inventario, venta, detalle_venta).
- Dominio `Usuario` + `Rol`, persistencia de usuario y `UsuarioTest` (3 pruebas).
- Registro `POST /api/usuarios` con BCrypt, login `POST /api/usuarios/login` con JWT y filtro JWT. Ya fue probado con Postman.
- En la base de datos ya existen usuarios de prueba (Juan Torres, Carlos Perez, Maria Lopez). **No borrarlos.**

## 6. TAREA: completar el backend para la Review 1

La review exige **mínimo 3 funcionalidades de extremo a extremo**. Ya existe un paquete con el código completo, preparado fuera de este equipo. **Ese código se compiló contra stubs, pero nunca se ejecutó con Spring real.** Tu trabajo es integrarlo, compilarlo, ejecutarlo, probarlo y corregir lo que falle, respetando la arquitectura.

**Ubicación del paquete:** `C:\Users\JUANESTC\Downloads\Kairos-backend\KAIROS_Review1\`

(Si no existe, pídele al usuario que descomprima `KAIROS_Review1.zip` en `C:\Users\JUANESTC\Downloads\Kairos-backend\`.)

### Paso 0: Git y GitHub (hacerlo primero; ver la sección 7)

- Antes se creó por error un repositorio Git en `C:\Users\JUANESTC` y ya se eliminó. Verifica con `git rev-parse --show-toplevel`: **debe dar la carpeta del backend**, nunca `C:/Users/JUANESTC`. Si da la carpeta personal, detente y avisa.
- Si la carpeta del backend no es un repositorio, ejecuta `git init` **dentro de ella**.
- Revisa que `.gitignore` incluya `target/`, `.idea/`, `*.iml` y `.vscode/`. Agrega lo que falte.
- Configura GitHub y crea el repositorio remoto como indica la sección 7.1.
- Commit del estado actual en `main`: `"feat(usuarios): registro, login con JWT y seguridad base"`, y haz `push`.
- Crea la rama `feature/review1-funcionalidades` y súbela.

### Paso 1: Integrar el código

1. Compara `src/main/java/com/kairos/Kairos_backend` (actual) con `KAIROS_Review1/Kairos-backend/src/main/java/com/kairos/Kairos_backend` (paquete). Explícale al usuario las diferencias principales.
2. Reemplaza el código Java por el del paquete. El paquete contiene la versión completa y coherente, incluidos los archivos de usuario rediseñados. Elimina los archivos viejos que el paquete movió o reemplazó; por ejemplo, los DTOs y `UsuarioController` sueltos en `adapter/in/web/` ahora están en `adapter/in/web/usuario/`. Si un archivo viejo tiene algo que el nuevo no tiene, avísale al usuario antes de borrarlo.
3. Copia `V2__datos_iniciales.sql` a `src/main/resources/db/migration/`. **No toques la V1.**
4. Reemplaza `src/main/resources/application.properties` por el del paquete.
5. Copia las pruebas `ProductoTest` e `InventarioTest` (y `UsuarioTest` si difiere) a `src/test/java/com/kairos/Kairos_backend/domain/model/`.

### Paso 2: Compilar y probar

```powershell
.\mvnw.cmd clean test
```

- Corrige los errores de compilación. Los puntos con más riesgo, porque no se verificaron con las librerías reales, son:
  - `SecurityConfig`: DSL de Spring Security 7.
  - `JwtService`: API de JJWT 0.13.
  - `InventarioJpaRepository`: consultas JPQL con `SELECT new ...InventarioDetalle(...)` y `JOIN ... ON`.
- `KairosBackendApplicationTests` necesita PostgreSQL encendido.
- **Meta:** todas las pruebas en verde (11 de dominio + contextLoads).

### Paso 3: Ejecutar y verificar

```powershell
.\mvnw.cmd spring-boot:run
```

En los logs debe aparecer:
- `now at version v2`
- `Administrador inicial creado: admin@kairos.com`
- `Started KairosBackendApplication`

Después verifica los endpoints con `Invoke-RestMethod` o `curl.exe` en PowerShell, o pídele al usuario que ejecute la colección `KAIROS_Review1/postman/KAIROS_Review1.postman_collection.json` (Postman → Run collection). Credenciales del admin: `admin@kairos.com` / `Admin123*`.

**Resultados esperados:**

| Petición | Código |
|---|---|
| `POST /api/usuarios` (registro público, siempre crea CLIENTE, sin `passwordHash`) | 201 |
| `POST /api/usuarios/login` | 200 + `token` |
| `GET /api/usuarios/me` sin token | 401 |
| `GET /api/usuarios` con token de cliente | 403 |
| `GET /api/usuarios` con token de admin | 200 |
| `POST /api/usuarios/empleados` (admin, rol VENDEDOR, `idAlmacen: 1`) | 201 |
| `GET /api/categorias`, `GET /api/productos?idCategoria=1&nombre=galaxy` (públicos) | 200 |
| `POST /api/productos` (admin) | 201 |
| `DELETE /api/productos/{id}` de un producto con inventario | 400 |
| `GET /api/inventario?idAlmacen=1` (admin) | 200, con los nombres de producto y almacén |
| `PATCH /api/inventario/{id}/ajuste` `{"tipo":"SALIDA","cantidad":9999}` | 400 "Stock insuficiente" |
| `GET /api/inventario/bajo-stock` (admin) | 200, incluye los productos que la V2 dejó bajo el mínimo |
| JSON inválido o campos vacíos | 400 con `campos` |
| Email repetido | 409 |

Las respuestas de error tienen siempre el formato `{status, error, mensaje, campos, fecha}`.

### Paso 4: Cerrar

- Haz los commits de la integración en la rama (ver 7.2), súbela y crea el **Pull Request** hacia `main` con `gh pr create`. Una vez que todo esté en verde, fusiónalo con `gh pr merge --merge`.
- Al final, dale al usuario un **resumen corto en español**: qué quedó funcionando, qué se corrigió, el enlace del repositorio y del Pull Request, y cómo explicar el flujo de una petición en la review:

```
Controller → UseCase → Service → Dominio → RepositoryPort → PersistenceAdapter → PostgreSQL
```

## 7. Git y GitHub (lo hace Claude Code)

### 7.1 Configuración inicial (solo una vez)

1. Revisa la identidad de Git con `git config user.name` y `git config user.email`. Si están vacías, configúralas: `git config --global user.name "Juan Esteban Torres"` y `git config --global user.email "tjuanes00@gmail.com"`.
2. Revisa si está instalado **GitHub CLI** con `gh --version`.
   - Si no está: `winget install --id GitHub.cli`. Después, pídele al usuario que cierre y abra VS Code para que se reconozca el comando.
3. Revisa la sesión con `gh auth status`. Si no hay sesión, **el usuario debe iniciarla él mismo** porque abre el navegador: pídele que ejecute en la terminal `gh auth login` y elija **GitHub.com → HTTPS → Login with a web browser**. Espera a que confirme.
4. Si el repositorio no tiene remoto (`git remote -v` vacío), créalo y súbelo:
   `gh repo create kairos-backend --private --source . --remote origin --push`
   - Pregunta **una sola vez** si lo quiere **privado** (recomendado) o **público**.
   - Si el remoto ya existe, solo haz `git push -u origin main`.
5. Opcional: pregúntale al usuario cuál es la cuenta de GitHub de su compañero Daniel para agregarlo como colaborador con `gh repo edit` o desde **Settings → Collaborators**.

### 7.2 Cómo hacer los commits

- **Un commit por cada paso que funcione**, no uno gigante al final. Por ejemplo:
  1. `chore(git): configurar .gitignore y repositorio`
  2. `feat(usuarios): roles, perfil, empleados y activar/desactivar`
  3. `feat(catalogo): categorías y productos con filtros`
  4. `feat(inventario): almacenes, stock por almacén y ajustes`
  5. `feat(seguridad): CORS, 401/403 y manejo global de errores`
  6. `feat(db): migración V2 con datos iniciales`
  7. `test(dominio): pruebas de Producto e Inventario`
  8. `fix: ...` por cada corrección que hagas al compilar o probar
- Formato: `tipo(alcance): descripción en español`. Tipos: `feat`, `fix`, `test`, `docs`, `chore`, `refactor`.
- **Antes de cada commit:** revisa `git status` para no incluir `target/`, `.idea/` ni archivos ajenos al proyecto. Solo haz commit si `.\mvnw.cmd clean test` compila (salvo el commit inicial del Paso 0).
- **Después de cada commit:** `git push`.
- **Nunca** uses `git push --force` ni reescribas el historial de `main`.
- Incluye este `CLAUDE.md` en el repositorio.
- Explícale al usuario, en una línea, qué es cada commit que hagas, para que aprenda a hacerlo solo.

## 8. Las 3 funcionalidades (referencia)

- **F1 Usuarios y autenticación (RF01, RF02):**
  - Registro de cliente, login con JWT (email, rol, id; expira en 1 h) y perfil (`/me`).
  - `PUT /api/usuarios/{id}` para el propio usuario o un admin.
  - `POST /api/usuarios/empleados`, solo admin.
  - Activar y desactivar usuarios, solo admin.
- **F2 Catálogo (RF03, RF05):**
  - Categorías (GET público; POST y PUT solo admin).
  - Productos (GET público con filtros `idCategoria` y `nombre`; POST, PUT y DELETE solo admin).
  - Reglas: precio ≥ 0, la categoría debe existir y no se elimina un producto que tenga inventario.
- **F3 Inventario (RF05):**
  - Almacenes (GET con sesión; POST, PUT y PATCH activar/desactivar solo admin).
  - Inventario: GET por almacén (admin y vendedor), registrar (admin), ajuste de ENTRADA o SALIDA (admin y vendedor), stock mínimo (admin) y bajo stock (admin).
  - Reglas: el stock nunca queda negativo, un solo registro por producto y almacén, y no se registra stock en un almacén inactivo.

## 9. Fuera de alcance por ahora

Ventas (RF04), garantías, traslados, jornadas, fidelización, reportes, IA y pagos: son los próximos incrementos. **No implementarlos en esta tarea.**
