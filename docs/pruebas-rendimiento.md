# Pruebas de carga y estrés (Apache JMeter)

## Qué es cada una

| Prueba | Pregunta que responde | Cómo se hace |
|---|---|---|
| **Carga** | ¿El sistema responde bien con la cantidad de usuarios **esperada**? | 50 empleados navegando 5 minutos, con 1 s de pausa entre clic y clic (como una persona real) |
| **Estrés** | ¿Hasta dónde aguanta y **cómo se rompe**? ¿Se recupera? | Sin pausas, subiendo por escalones (50 → 100 → 200 → 400 → 800 → 1500 usuarios), 1 minuto cada uno |

## Escenario

Cada usuario virtual es un **empleado** que inicia sesión **una vez** (`POST /api/usuarios/login`, con JWT) y luego repite, hasta que se acaba el tiempo:

catálogo → buscar producto → categorías → inventario de una sede → bajo stock → ventas → mi perfil

Son las consultas más usadas en el día a día. No se crean ventas ni se cambia el inventario: la prueba no ensucia los datos.

## Archivos

| Archivo | Para qué |
|---|---|
| [`pruebas-rendimiento/kairos.jmx`](../pruebas-rendimiento/kairos.jmx) | El plan de JMeter. Usuarios, duración y pausas son parámetros: el mismo plan sirve para carga y estrés |
| [`pruebas-rendimiento/ejecutar.ps1`](../pruebas-rendimiento/ejecutar.ps1) | Corre JMeter sin ventana, genera el reporte HTML, mide CPU y memoria del contenedor del backend y muestra un resumen |
| `pruebas-rendimiento/resultados/` | Reportes de cada ejecución (no se suben a GitHub: pesan mucho) |

## Cómo ejecutarlas

1. **Instalar JMeter** (una sola vez): descargar `apache-jmeter-5.6.3.zip` de https://jmeter.apache.org/download_jmeter.cgi y descomprimirlo en `C:\Users\<tu usuario>\herramientas\`. Si lo pones en otro lado, crea la variable `JMETER_HOME` con esa ruta.
2. **Arrancar el backend**: `docker compose up -d` (o la imagen publicada: `docker compose -f docker-compose.github.yml up -d db backend`).
3. **Ejecutar**:

```powershell
.\pruebas-rendimiento\ejecutar.ps1 -Tipo carga      # unos 5 minutos
.\pruebas-rendimiento\ejecutar.ps1 -Tipo estres     # unos 7 minutos
.\pruebas-rendimiento\ejecutar.ps1 -Tipo estres -Usuarios 1500   # un escalón concreto
```

4. **Ver el reporte**: abrir `pruebas-rendimiento\resultados\<fecha>-<tipo>-<usuarios>u\reporte\index.html` en el navegador.

- La clave del usuario sale de `ADMIN_PASSWORD` en `.env` (o de la variable `KAIROS_CLAVE`). No se escribe en el plan ni en la línea de comandos: el script la pasa en un archivo temporal que borra al terminar.
- Para **ver o editar** el plan con ventanas: `herramientas\apache-jmeter-5.6.3\bin\jmeter.bat` → *File → Open* → `kairos.jmx`. **No corras las pruebas desde la ventana**: gasta mucha memoria y altera los resultados.

## Resultados (17 de noviembre de 2026)

Ambiente: imagen publicada `ghcr.io/juanes-torres/kairos-backend:produccion` (v2026.11.17-6) y PostgreSQL 18 en Docker Desktop, en un PC con 8 núcleos y 6 GB para Docker. **JMeter corrió en el mismo PC**, así que le quita CPU al backend: en un servidor aparte los números serían mejores.

*p95 = el 95 % de las peticiones respondió en ese tiempo o menos.*

### Carga: 50 usuarios, 5 minutos, 1 s de pausa

| Peticiones | Por segundo | Promedio | p95 | p99 | Errores | CPU máx. del backend |
|---|---|---|---|---|---|---|
| 13 336 | 44,6 | **5 ms** | **10 ms** | 29 ms | **0 %** | 74 % (de 800 %) |

| Petición | Promedio | p95 |
|---|---|---|
| Consultas (catálogo, búsqueda, categorías, inventario, bajo stock, ventas, perfil) | 4–5 ms | 8–11 ms |
| Login | 113 ms | 225 ms |

**Resultado: aprobada.** El login es lo más lento **a propósito**: BCrypt está diseñado para tardar y así frena a quien intente adivinar contraseñas.

### Estrés: sin pausas, por escalones de 1 minuto

| Usuarios | Peticiones | Por segundo | Promedio | p95 | p99 | Máximo | Errores | CPU máx. |
|---|---|---|---|---|---|---|---|---|
| 50 | 62 488 | 1 044 | 44 ms | 94 ms | 127 ms | 0,5 s | 0 % | 301 % |
| 100 | 65 848 | 1 099 | 83 ms | 224 ms | 291 ms | 1,4 s | 0 % | 669 % |
| 200 | 71 941 | **1 199** | 152 ms | 447 ms | 560 ms | 1,5 s | 0 % | 488 % |
| 400 | 61 228 | 1 016 | 361 ms | 844 ms | 995 ms | 7,9 s | 0 % | 707 % |
| 800 | 49 298 | 806 | 899 ms | 1,3 s | 1,8 s | 14,5 s | 0 % | 800 % |
| 1500 | 12 566 | 202 | 6,7 s | 25,9 s | 30 s | 30 s | **16 %** | 912 % |

*(CPU de Docker: 100 % = un núcleo; 800 % = los 8 núcleos al máximo.)*

## Conclusiones

1. **Capacidad máxima: unas 1 200 peticiones por segundo**, alcanzada con 200 usuarios sin pausas. Una persona real hace cerca de una petición por segundo, así que eso equivale, en teoría, a **cientos de empleados trabajando a la vez**. Es mucho más de lo que necesita una franquicia.
2. **Punto de saturación: más de 200 usuarios sin pausas.** Desde ahí el CPU está al máximo: atender más usuarios solo hace que cada uno espere más, y las peticiones por segundo bajan.
3. **Punto de quiebre: entre 800 y 1 500 usuarios iniciando sesión al mismo tiempo.** Con 1 500, el 28 % de los logins pasó los 30 s y se cortó. Esos usuarios quedaron sin token, y sus consultas siguientes respondieron 401. **El cuello de botella es el CPU que usa BCrypt en el login**, no la base de datos ni la memoria.
4. **El sistema se degrada sin caerse y se recupera solo.** En ningún escalón se cayó el backend ni respondió 500. La memoria se mantuvo estable (≈ 660 MB) y, al terminar cada escalón, el CPU volvió a menos de 1 %.

## Mejoras posibles (no implementadas)

- **Limitar los intentos de login** por IP o por usuario: protege de ataques de fuerza bruta y evita que muchos logins juntos acaparen el CPU.
- **Más copias del backend detrás de un balanceador.** Es posible porque el backend no guarda estado (JWT, 12 factores VI y VIII).
- **Caché del catálogo y de las categorías**: cambian poco y son lo que más se consulta.
- **Correr JMeter en otra máquina**, para medir sin que le robe CPU al backend.
