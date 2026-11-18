# Fotos de los productos (SCRUM-103)

Cada producto puede tener **una foto**. El administrador la sube desde su PC o la toma del catálogo de códigos de barras, y cualquiera la ve en el catálogo.

## Endpoints

| Método y ruta | Quién | Qué hace |
|---|---|---|
| `GET /api/productos/{id}/imagen` | Público | Devuelve la foto (`image/jpeg`, `image/png` o `image/webp`). 404 si no tiene |
| `POST /api/productos/{id}/imagen` | ADMINISTRADOR de la empresa | Sube o reemplaza la foto. **form-data**, campo `archivo` |
| `POST /api/productos/{id}/imagen/codigo-barras` | ADMINISTRADOR de la empresa | `{"codigoBarras": "190199098572"}`: usa la primera foto que sirva de las que ofrece UPCitemdb |
| `DELETE /api/productos/{id}/imagen` | ADMINISTRADOR de la empresa | Quita la foto (204) |

- **El catálogo ya trae la dirección de la foto.** `GET /api/productos` y `GET /api/productos/{id}` incluyen `imagenUrl`, por ejemplo `/api/productos/15/imagen?v=1a2b3c`, o `null` si no hay foto. El `?v=` cambia cada vez que cambia la foto, así el navegador nunca muestra la anterior.
- **La consulta por código de barras sugiere fotos.** `GET /api/productos/codigo-barras/{codigo}` trae `imagenes`, una lista de direcciones, para mostrarlas antes de elegir.

## Reglas

- Solo **JPG, PNG o WEBP**. Se revisa el **contenido real** del archivo (sus primeros bytes), no el nombre: un archivo renombrado a `.png` no pasa.
- Máximo **2 MB**. Si se pasa: 400 *"La imagen no puede pesar más de 2 MB"*.
- **Un administrador de otra empresa** recibe 404, como si el producto no existiera. **Un vendedor** recibe 403.
- **Al cambiar o quitar la foto, se borra el archivo anterior.**
- **Las fotos del código de barras se descargan y se guardan en KAIRÓS.** Así no dependen de que la otra página siga existiendo.

## Dónde se guardan

| | |
|---|---|
| Archivo | En la carpeta `ARCHIVOS_CARPETA` (por defecto `./archivos`; en Docker, el volumen `kairos-archivos` en `/app/archivos`) |
| Base de datos | Solo la ubicación, en la columna `producto.imagen` (migración V7), por ejemplo `productos/15-<uuid>.jpg` |

**Arquitectura hexagonal:** el caso de uso (`ImagenProductoService`) usa el puerto `AlmacenamientoArchivosPort` y no sabe que detrás hay una carpeta.
- Para pasar a un servicio en la nube (Amazon S3, Cloudinary) basta escribir otro adaptador.
- Ese cambio hará falta si algún día hay varias copias del backend en servidores distintos: todas deben ver las mismas fotos (12 factores: VI).
- El mismo puerto servirá para las fotos de **Créditos**.

## Seguridad

- **Ninguna ruta puede salirse de la carpeta de archivos**: `../` se rechaza.
- **Las fotos del código de barras se descargan con protección SSRF** (`HttpDescargaImagenesAdapter`). Las direcciones vienen de una API externa, así que alguien podría intentar que KAIRÓS pida páginas de su propia red interna. Por eso el adaptador:
  - solo acepta `http` y `https`;
  - nunca pide a direcciones privadas o locales (127.x, 10.x, 192.168.x, 169.254.x…);
  - no sigue redirecciones;
  - corta en 2 MB y con un tiempo máximo de espera.
- **El servidor rechaza envíos de más de 3 MB** con el mismo mensaje claro (`server.tomcat.max-swallow-size`), sin cortar la conexión.

## Para el frontend (Daniel)

1. **En la tarjeta del catálogo:** si `imagenUrl` no es `null`, `<img src={apiUrl + producto.imagenUrl}>`. Si es `null`, se deja el recuadro "KAIRÓS" que ya existe.
2. **En la administración de productos**, un botón "Subir foto":

```ts
const datos = new FormData();
datos.append("archivo", archivoElegido);          // <input type="file" accept="image/jpeg,image/png,image/webp">
await fetch(`${apiUrl}/api/productos/${id}/imagen`, {
  method: "POST",
  headers: { Authorization: `Bearer ${token}` },  // SIN Content-Type: el navegador lo pone con el "boundary"
  body: datos,
});
```

3. **Opcional:** al consultar un código de barras, mostrar las `imagenes` sugeridas y un botón "Usar foto del código" que llame a `POST /api/productos/{id}/imagen/codigo-barras`.

En Postman están en la carpeta **2. Catálogo** (subir, ver, quitar) y en **6. Código de barras** (usar la foto del código).
