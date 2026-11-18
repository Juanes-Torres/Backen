# Ver los logs de KAIRÓS con Grafana + Loki

El backend escribe sus logs en la consola (12 factores, XI). Para buscarlos y filtrarlos en el navegador se usan tres herramientas en Docker (carpeta `observabilidad/`):

```
backend en Docker ──consola JSON──┐
backend local ──logs/kairos.log──┴──▶ Alloy ──envía──▶ Loki ──consulta──▶ Grafana (navegador)
```

| Herramienta | Para qué sirve | Dirección |
|---|---|---|
| **Alloy** | Lee la consola del contenedor `kairos-backend` y el archivo de log local, y se los pasa a Loki | (interna) |
| **Loki** | Guarda los logs y permite buscarlos | http://localhost:3100 |
| **Grafana** | Muestra los logs con filtros y gráficas | http://localhost:3300 |

## Cómo usarlo

1. Arranca el backend de una de estas dos formas (sirven las dos a la vez):
   - **En Docker** (carpeta del backend): `docker compose up -d`. Escribe sus logs en JSON por la consola y Alloy los lee directo de Docker.
   - **Local, con archivo de log**:
     ```powershell
     $env:LOG_FILE = "logs/kairos.log"; .\mvnw.cmd spring-boot:run
     ```
2. Arranca las herramientas (en la carpeta `observabilidad`):
   ```powershell
   docker compose up -d
   ```
3. Abre http://localhost:3300 → usuario `admin`, contraseña `admin` (la primera vez pide cambiarla; se puede saltar con *Skip*).
4. Menú **Dashboards** → carpeta **KAIRÓS** → **KAIRÓS - Logs**.

Para apagarlas: `docker compose down` (los logs guardados se conservan). Para borrarlos también: `docker compose down -v`.

## Qué muestra el tablero

- **Peticiones**: cuántas atendió el backend en el rango de tiempo elegido.
- **Avisos y errores**: líneas WARN o ERROR (en rojo si hay alguna).
- **Peticiones por código HTTP**: cuántas respondieron 200, 400, 401, 404...
- **Logs**: cada línea con su nivel, `[idPeticion u=usuario e=empresa]` y el mensaje.

Arriba hay filtros: **Origen** (`docker` o `local`), **Nivel**, **Id de petición**, **Usuario** y **Empresa** (`.*` = todos). Ejemplo: escribe `1` en Empresa para ver solo lo que hizo la empresa 1.

## Rastrear una petición

Cada respuesta del backend trae el encabezado `X-Request-Id`. Si el frontend o Postman reportan un error, se copia ese id, se pega en el filtro **Id de petición** y aparecen todas las líneas de esa petición, incluido el correo que se envía en segundo plano.

## Variables de logs del backend

| Variable | Valor por defecto | Para qué |
|---|---|---|
| `LOG_LEVEL` | `INFO` | Nivel de los logs de KAIRÓS (`DEBUG`, `INFO`, `WARN`, `ERROR`) |
| `LOG_FORMAT` | `ecs` en pre-producción y producción | Logs de consola en JSON |
| `LOG_FILE` | vacío (sin archivo) | Archivo de log en JSON; solo hace falta para Grafana + Loki |
