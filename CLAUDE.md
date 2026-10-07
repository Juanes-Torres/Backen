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

- Haz los commits de la integración en la rama (ver 7.2), súbela y crea el **Pull Request** hacia `desarrollo` con `gh pr create` (ver 7.3). Una vez que todo esté en verde, fusiónalo con `gh pr merge --merge`.
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
- Formato: `tipo: descripción corta en español`, **al grano y sin tecnicismos** (el usuario lo pidió así). Tipos: `feat`, `fix`, `test`, `docs`, `chore`, `refactor`. Ejemplo: `feat: script para ver y elegir el ambiente`.
- **Antes de cada commit:** revisa `git status` para no incluir `target/`, `.idea/` ni archivos ajenos al proyecto. Solo haz commit si `.\mvnw.cmd clean test` compila (salvo el commit inicial del Paso 0).
- **Después de cada commit:** `git push`.
- **Nunca** uses `git push --force` ni reescribas el historial de `produccion`, `pre-produccion`, `desarrollo` ni `main`.
- Incluye este `CLAUDE.md` en el repositorio.
- Explícale al usuario, en una línea, qué es cada commit que hagas, para que aprenda a hacerlo solo.
- **No** agregues `Co-Authored-By: Claude` ni "Generated with Claude Code" en commits ni Pull Requests.

### 7.3 Ramas y ambientes

Repositorio: `https://github.com/Juanes-Torres/Backen`. La rama principal (default) es `produccion`.

```
feature/xxx  →  desarrollo  →  pre-produccion  →  produccion
```

| Rama | Ambiente | Qué recibe |
|---|---|---|
| `desarrollo` | Desarrollo | Pull Requests de las ramas `feature/...`, `fix/...` y `docs/...` |
| `pre-produccion` | Pruebas antes de publicar | Pull Request desde `desarrollo` cuando está estable |
| `produccion` | Versión final (default) | Pull Request desde `pre-produccion` cuando todo está probado |

- Las ramas de trabajo salen **siempre de `desarrollo`** (`git switch desarrollo; git pull; git switch -c feature/xxx`).
- Los Pull Requests de trabajo van **hacia `desarrollo`**, nunca directo a `produccion`.
- Para publicar: PR `desarrollo` → `pre-produccion`, se prueba (`.\mvnw.cmd test` + endpoints), y luego PR `pre-produccion` → `produccion`.
- En GitHub solo quedan fijas `desarrollo`, `pre-produccion` y `produccion` (la antigua `main` se borró el 2026-10-06).
- Las ramas `feature/...`, `fix/...` y `docs/...` son temporales: GitHub las borra solo al fusionar el PR (opción "delete branch on merge" activada). Después borra también la copia local con `git branch -d`.

### 7.4 Configuración por ambiente (perfiles de Spring Boot)

| Archivo | Uso |
|---|---|
| `application.properties` | Lo común a todos. Perfil por defecto: `desarrollo`. Puerto: `SERVER_PORT` (opcional, por defecto 8080) |
| `application-desarrollo.properties` | Local, con valores por defecto (`kairos_db`, `show-sql=true`) |
| `application-pre-produccion.properties` / `application-produccion.properties` | Sin valores por defecto: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` son obligatorias; si falta una, la app no arranca |

- Elegir perfil: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=produccion"` o `SPRING_PROFILES_ACTIVE=produccion`.
- Nunca escribir secretos reales en los `.properties` de pre-producción ni de producción.
- `scripts\ambiente.ps1` muestra los ambientes y sus variables (secretos ocultos) y arranca el backend: `.\scripts\ambiente.ps1 -Ambiente produccion -SoloMostrar`.

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

## 10. TAREA 2: correo de bienvenida al registrarse (Gmail)

**Qué pidió el usuario:** cuando alguien se registra, el sistema le envía un correo diciéndole que quedó registrado en KAIRÓS. Esto corresponde a la dependencia "Servicio de Notificaciones (email)" de la Tabla 5 del Documento V1.

### 10.1 Decisión técnica (explícasela al usuario en 3 líneas)

- Se usa el **servidor de correo de Gmail (SMTP de Google)** con una **Contraseña de aplicación** de Google, a través de `spring-boot-starter-mail`.
- Se descartó la **Gmail API con OAuth2** para esta etapa: exige proyecto en Google Cloud, pantalla de consentimiento y tokens de actualización. Es mucho más complejo para el mismo resultado. Queda como mejora futura.
- En hexagonal es otro **puerto de salida** (`NotificacionPort`) con su **adaptador** (`GmailNotificacionAdapter`). El caso de uso no sabe que existe Gmail.

### 10.2 Reglas de esta tarea

- **Rama:** `feature/correo-bienvenida`, creada desde `desarrollo` actualizado. PR hacia `desarrollo`.
- **Si el correo falla, el registro NO debe fallar:** se registra un aviso en el log y el usuario queda creado igual.
- **El envío es asíncrono** (`@Async`), para que la respuesta del registro no espere a Gmail.
- **Con el correo deshabilitado** (`kairos.mail.enabled=false`) la app debe arrancar y las pruebas deben pasar sin credenciales. Para eso se usa un adaptador "deshabilitado" que solo escribe en el log.
- **Secretos:**
  - La contraseña de aplicación **nunca** va en archivos versionados.
  - En desarrollo va en `secrets.properties` (raíz del proyecto, **agregarlo al `.gitignore`**).
  - En pre-producción y producción va en variables de entorno.
- **Dependencia nueva:** `spring-boot-starter-mail`. El usuario ya la aprobó al pedir esta funcionalidad. Explica para qué es.
- **Escapa el nombre del usuario** en el HTML (`HtmlUtils.htmlEscape`) para evitar inyección de HTML.

### 10.3 Archivos

**1. `pom.xml`** — agregar dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

**2. `application/port/out/NotificacionPort.java`**

```java
package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * PUERTO DE SALIDA: avisarle algo al usuario (hoy por correo).
 * El caso de uso no sabe si por detrás hay Gmail, otro proveedor o nada.
 */
public interface NotificacionPort {

    void enviarBienvenida(Usuario usuario);
}
```

**3. `application/usecase/RegistrarUsuarioService.java`**

Inyectar `NotificacionPort` en el constructor y, **después de guardar**, notificar:

```java
Usuario guardado = usuarioRepository.guardar(nuevo);
notificacionPort.enviarBienvenida(guardado);   // asíncrono: no bloquea ni hace fallar el registro
return guardado;
```

**4. `infrastructure/adapter/out/notificacion/GmailNotificacionAdapter.java`**

```java
package com.kairos.Kairos_backend.infrastructure.adapter.out.notificacion;

import com.kairos.Kairos_backend.application.port.out.NotificacionPort;
import com.kairos.Kairos_backend.domain.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;

/**
 * ADAPTADOR de NotificacionPort que envía correos con el servidor SMTP de Gmail.
 * Solo se activa si kairos.mail.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "kairos.mail.enabled", havingValue = "true")
public class GmailNotificacionAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(GmailNotificacionAdapter.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String nombreRemitente;
    private final String urlFrontend;

    public GmailNotificacionAdapter(JavaMailSender mailSender,
                                    @Value("${spring.mail.username}") String remitente,
                                    @Value("${kairos.mail.from-name:KAIRÓS}") String nombreRemitente,
                                    @Value("${kairos.frontend.url:http://localhost:5173}") String urlFrontend) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
        this.urlFrontend = urlFrontend;
    }

    @Async
    @Override
    public void enviarBienvenida(Usuario usuario) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setFrom(remitente, nombreRemitente);
            helper.setTo(usuario.getEmail());
            helper.setSubject("¡Bienvenido a KAIRÓS!");
            helper.setText(plantilla(usuario), true);   // true = contenido HTML
            mailSender.send(mensaje);
            log.info("Correo de bienvenida enviado a {}", usuario.getEmail());
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            // El registro ya quedó guardado: solo se deja constancia del fallo
            log.warn("No se pudo enviar el correo de bienvenida a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }

    private String plantilla(Usuario usuario) {
        String nombre = HtmlUtils.htmlEscape(usuario.getNombre());
        String email = HtmlUtils.htmlEscape(usuario.getEmail());
        return """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;border:1px solid #d9dee5;border-radius:10px;overflow:hidden">
                  <div style="background:#1f4e79;color:#fff;padding:20px 24px;font-size:24px;font-weight:bold;letter-spacing:2px">KAIRÓS</div>
                  <div style="padding:24px;color:#1f2933;line-height:1.5">
                    <h2 style="margin-top:0">¡Hola, %s!</h2>
                    <p>Tu cuenta en <strong>KAIRÓS</strong> se creó correctamente.</p>
                    <p><strong>Usuario:</strong> %s<br><strong>Rol:</strong> %s</p>
                    <p>Ya puedes iniciar sesión, consultar nuestro catálogo y revisar tus compras.</p>
                    <p style="text-align:center;margin:28px 0">
                      <a href="%s/login" style="background:#1f4e79;color:#fff;padding:12px 22px;border-radius:6px;text-decoration:none">Iniciar sesión</a>
                    </p>
                    <p style="font-size:12px;color:#6b7280">Si no creaste esta cuenta, ignora este mensaje.</p>
                  </div>
                </div>
                """.formatted(nombre, email, usuario.getRol().name(), urlFrontend);
    }
}
```

**5. `infrastructure/adapter/out/notificacion/NotificacionDeshabilitadaAdapter.java`**

```java
package com.kairos.Kairos_backend.infrastructure.adapter.out.notificacion;

import com.kairos.Kairos_backend.application.port.out.NotificacionPort;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Se usa cuando el correo está apagado (kairos.mail.enabled=false):
 * la app funciona igual y solo deja constancia en el log.
 */
@Component
@ConditionalOnProperty(name = "kairos.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NotificacionDeshabilitadaAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacionDeshabilitadaAdapter.class);

    @Override
    public void enviarBienvenida(Usuario usuario) {
        log.info("Correo deshabilitado: no se envía la bienvenida a {}", usuario.getEmail());
    }
}
```

**6. `infrastructure/config/AsyncConfig.java`**

```java
package com.kairos.Kairos_backend.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Activa @Async: el correo se envía en segundo plano. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
```

**7. Configuración** (respetando los perfiles de la sección 7.4)

- **`application.properties`** (común):

  ```properties
  # ===== Correo (servidor SMTP de Gmail) =====
  kairos.mail.enabled=${MAIL_ENABLED:false}
  kairos.mail.from-name=KAIRÓS
  spring.mail.host=smtp.gmail.com
  spring.mail.port=587
  spring.mail.username=${MAIL_USERNAME:}
  spring.mail.password=${MAIL_PASSWORD:}
  spring.mail.properties.mail.smtp.auth=true
  spring.mail.properties.mail.smtp.starttls.enable=true
  ```

- **`application-desarrollo.properties`**:

  ```properties
  # Secretos locales (NO se suben a GitHub)
  spring.config.import=optional:file:./secrets.properties
  kairos.frontend.url=http://localhost:5173
  ```

- **`secrets.properties.example`** (este sí se sube, como plantilla):

  ```properties
  # Copia este archivo como secrets.properties y llena tus datos. secrets.properties NO se sube a GitHub.
  kairos.mail.enabled=true
  spring.mail.username=tu_correo@gmail.com
  spring.mail.password=xxxx xxxx xxxx xxxx
  ```

- **`.gitignore`**: agregar `secrets.properties`.
- **Pre-producción y producción:** agregar `kairos.frontend.url=${FRONTEND_URL}` a sus `.properties`. Documenta en `scripts\ambiente.ps1` las variables nuevas `MAIL_ENABLED`, `MAIL_USERNAME`, `MAIL_PASSWORD` y `FRONTEND_URL` (secretos ocultos).

**8. Prueba unitaria** `src/test/java/.../application/usecase/RegistrarUsuarioServiceTest.java`

Usa **clases falsas escritas a mano** (implementaciones simples de los puertos, sin Mockito ni base de datos) y comprueba:

- Al registrar, se llama a `NotificacionPort.enviarBienvenida` **una vez**, con el usuario ya guardado.
- Si el email ya existe, **no** se envía correo.

Esto demuestra en la review la ventaja de los puertos: el caso de uso se prueba sin Gmail.

### 10.4 Lo que hace el usuario (guíalo paso a paso cuando llegues aquí)

1. **Crear la contraseña de aplicación de Google.** Conviene usar una cuenta de Gmail del proyecto, por ejemplo `kairos.notificaciones@gmail.com`, y no la personal.
   1. Ir a `myaccount.google.com` → **Seguridad** → activar **Verificación en 2 pasos**. Es obligatoria para poder crear contraseñas de aplicación.
   2. Ir a `myaccount.google.com/apppasswords` → nombre: `KAIROS` → **Crear**.
   3. Copiar los **16 caracteres** que muestra Google. Solo se ven una vez.
2. **Crear `secrets.properties`** a partir de `secrets.properties.example` y llenar el correo y la contraseña de aplicación. **Nunca** pegues esa contraseña en el chat ni en archivos que se suban a GitHub.

### 10.5 Verificación

1. **Sin `secrets.properties`:** `.\mvnw.cmd clean test` en verde. La app arranca y al registrar aparece en el log "Correo deshabilitado…".
2. **Con `secrets.properties`:** registrar por Postman un cliente con un **correo real del usuario** → **201** inmediato. En el log sale "Correo de bienvenida enviado a…" y el correo llega (revisar también **Spam**).
3. **Con una contraseña incorrecta a propósito:** el registro sigue respondiendo **201** y el log muestra "No se pudo enviar…".
4. **`git status`** no muestra `secrets.properties`.

### 10.6 Commits sugeridos (estilo de la sección 7.2)

1. `feat: enviar correo de bienvenida al registrarse`
2. `test: probar que el registro avisa por correo`
3. `docs: explicar cómo configurar el correo`

Luego: push, PR hacia `desarrollo`, y un resumen en español para el usuario.
