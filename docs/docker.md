# Ejecutar KAIRÓS con Docker

Con Docker se levantan **PostgreSQL y el backend juntos** con un solo comando, sin instalar Java, Maven ni PostgreSQL. Sirve para que Daniel pruebe el frontend contra el backend, para la demostración y para publicar en un servidor.

## Qué hay en el proyecto

| Archivo | Para qué sirve |
|---|---|
| `Dockerfile` | La receta de la imagen del backend. Tiene dos etapas: la primera compila con Maven y la segunda solo ejecuta con Java 21, para que la imagen sea más liviana |
| `docker-compose.yml` | Levanta dos contenedores: `kairos-db` (PostgreSQL 18) y `kairos-backend` |
| `.env.example` | Plantilla de las variables y los secretos. Se copia como `.env`, que **no** se sube a GitHub |
| `.dockerignore` | Lo que Docker no copia a la imagen: `target/`, `.git/`, los secretos… |

## Requisito: Docker Desktop

1. Descarga **Docker Desktop** desde docker.com e instálalo. En Windows 11 Home usa **WSL 2**; el instalador lo activa y puede pedir reiniciar.
2. Abre Docker Desktop y espera a que diga **"Engine running"**.
3. Comprueba en una terminal: `docker --version` y `docker compose version`.

## Levantar el sistema

```powershell
# 1. Solo la primera vez: crear el archivo de secretos
copy .env.example .env
#    ...y cambia DB_PASSWORD, JWT_SECRET (mínimo 32 caracteres) y ADMIN_PASSWORD

# 2. Construir y arrancar (la primera vez tarda varios minutos descargando todo)
docker compose up --build
```

Cuando en los logs aparezca `Started KairosBackendApplication`:
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Administrador: el `ADMIN_EMAIL` y el `ADMIN_PASSWORD` de tu `.env`

## Comandos útiles

| Comando | Qué hace |
|---|---|
| `docker compose up --build -d` | Arranca en segundo plano (`-d`) |
| `docker compose logs -f backend` | Muestra los logs del backend en vivo |
| `docker compose ps` | Muestra qué contenedores están corriendo |
| `docker compose down` | Detiene todo. **Los datos se conservan** |
| `docker compose down -v` | Detiene todo y **borra la base de datos** de Docker (empieza de cero) |

## Cosas a tener en cuenta

- **La base de datos de Docker es otra**, distinta del PostgreSQL de tu computadora. Empieza vacía y Flyway crea las tablas (V1, V2, V3…) y los datos de prueba al arrancar. Tus usuarios locales (Juan Torres, Carlos Perez…) **no** aparecen ahí.
- **Puertos:** PostgreSQL de Docker queda en el **5433**, porque el 5432 lo usa tu PostgreSQL local. Para conectarte con pgAdmin o DBeaver usa `localhost:5433`, base `kairos_db`. El backend queda en el **8080**: si ya tienes el backend corriendo con `mvnw`, detenlo antes o cambia `SERVER_PORT` en el `.env`.
- **Correo:** dentro de Docker no se lee `secrets.properties`. Para activarlo, llena `MAIL_ENABLED=true`, `MAIL_USERNAME` y `MAIL_PASSWORD` en el `.env`.
- **Pruebas:** la imagen se construye sin correr las pruebas, porque la prueba de arranque necesita PostgreSQL. Antes de subir cambios, sigue corriendo `.\mvnw.cmd clean test`.
- **Producción:** pon `SPRING_PROFILES_ACTIVE=produccion` en el `.env` y llena todas las variables con valores reales y seguros.
