# CI/CD del backend de KAIRÓS

Todo está en un solo archivo: [`.github/workflows/ci-cd.yml`](../.github/workflows/ci-cd.yml). Lo ejecuta **GitHub Actions**, en servidores de GitHub, sin que nadie lo corra a mano.

## Qué es cada cosa

| | Qué significa | Qué hace en KAIRÓS |
|---|---|---|
| **CI** (integración continua) | Cada cambio se prueba solo, antes de juntarlo con el resto | En cada Pull Request y cada push a `desarrollo`, `pre-produccion` y `produccion` corre **todas las pruebas** (unitarias y de integración, con PostgreSQL 18 real) y exige **80 % de cobertura** (JaCoCo) |
| **CD** (entrega continua) | Si las pruebas pasan, el sistema queda **empaquetado y listo para instalar** sin pasos manuales | En `pre-produccion` y `produccion` construye la **imagen de Docker** y la publica en **GitHub Container Registry** (GHCR). En `produccion` además le pone **número de versión** y crea una **Release** |

## El recorrido de un cambio

```
feature/xxx ──PR──▶ desarrollo ──PR──▶ pre-produccion ──PR──▶ produccion
     │                  │                    │                    │
     CI                 CI                CI + CD              CI + CD
  (pruebas)          (pruebas)      imagen :pre-produccion   imagen :produccion
                                                             + :latest + versión
                                                             + Release en GitHub
```

1. **CI (trabajo "Pruebas y cobertura")**: levanta PostgreSQL 18, corre `./mvnw verify` y escribe en el resumen cuántas pruebas pasaron y la cobertura. Si algo falla, el PR muestra una **X roja**.
2. **CD (trabajo "Publicar imagen Docker")**: tiene `needs: pruebas`, así que **solo arranca si el CI pasó**. Solo corre en un *push* (el merge de un PR) a `pre-produccion` o `produccion`, nunca en un PR.
   - Construye la imagen con el mismo `Dockerfile` que se usa en el PC.
   - La sube a `ghcr.io/juanes-torres/kairos-backend` con estas etiquetas:

| Etiqueta | Cuándo | Para qué |
|---|---|---|
| `:pre-produccion` / `:produccion` | Siempre | La última versión de cada ambiente |
| `:sha-abc1234` | Siempre | El commit exacto con el que se construyó |
| `:v2026.11.17-12` | Solo produccion | La versión: fecha + número de ejecución. Nunca cambia, sirve para volver atrás |
| `:latest` | Solo produccion | La última versión publicada |

3. En `produccion` crea una **Release** en GitHub (pestaña *Releases*) con la lista de Pull Requests que entraron y el comando para descargar esa versión.

Cada ejecución del CD queda registrada en la pestaña **Deployments** del repositorio, separada por ambiente.

## No necesita secretos

Para publicar, GitHub entrega una clave temporal (`GITHUB_TOKEN`) que solo existe mientras corre el workflow. El archivo pide solo los permisos necesarios: `packages: write` (subir la imagen) y `contents: write` (crear la Release). Las claves de la aplicación (base de datos, JWT, correo, Anthropic) **no van en la imagen**: se pasan como variables de entorno al ejecutarla (12 factores: III).

## Cómo usar la imagen publicada

```powershell
docker pull ghcr.io/juanes-torres/kairos-backend:produccion
```

O todo el sistema con [`docker-compose.github.yml`](../docker-compose.github.yml) (ver [docker.md](docker.md)): descarga esta imagen en vez de compilar el backend. Para otra versión: `KAIROS_VERSION=pre-produccion` o `KAIROS_VERSION=v2026.11.17-12`.

**Volver a una versión anterior** (si una publicación sale mal): `KAIROS_VERSION=<versión anterior>` y `docker compose -f docker-compose.github.yml up -d`.

## Dónde verlo en GitHub

| Qué | Dónde |
|---|---|
| Ejecuciones y resultados | Pestaña **Actions** → *CI/CD backend* |
| Imágenes publicadas | Página del repositorio → **Packages** → *kairos-backend* |
| Versiones | Pestaña **Releases** |
| Historial por ambiente | **Deployments** (columna derecha de la página del repositorio) |
| Reporte de cobertura | En cada ejecución, abajo: artefacto `cobertura-jacoco` |

## Correrlo a mano

Pestaña **Actions** → *CI/CD backend* → **Run workflow**. Corre solo el CI: el CD se activa únicamente con un merge a `pre-produccion` o `produccion`.
