# Guía para el frontend: datos para una interfaz más profesional

Lo que ofrece el backend para que cada empresa vea **su marca**, tenga un **panel de inicio** con números y el catálogo se vea como el de una tienda en línea. Todas las rutas de imágenes son **relativas al backend**: se usan como `apiUrl + ruta`.

## 1. Marca de la empresa (SCRUM-105)

**Qué cambia en pantalla:** un empleado ve **el nombre y el logo de su empresa** en la barra superior y en las facturas, en lugar de "KAIRÓS". KAIRÓS queda como la plataforma ("con tecnología de KAIRÓS" en el pie, por ejemplo).

| Ruta | Quién | Para qué |
|---|---|---|
| `POST /api/usuarios/login` | Todos | Ya trae `empresa` con `nombre`, `nit` y **`logoUrl`** (nulo si no tiene logo) |
| `GET /api/empresas/mi-empresa` | Admin y vendedor | Lo mismo, para refrescar la barra al recargar la página o después de editar |
| `PUT /api/empresas/mi-empresa` | Admin | `{"nombre", "email", "telefono"}`. El NIT no cambia |
| `POST /api/empresas/mi-empresa/logo` | Admin | `FormData` con el campo `archivo` (JPG, PNG o WEBP, máximo 2 MB) |
| `DELETE /api/empresas/mi-empresa/logo` | Admin | Quita el logo (204) |
| `GET /api/empresas/{id}/logo` | Público | La imagen. Solo de empresas activas |
| `GET /api/empresas/activas` | Público | Cada empresa trae `logoUrl`, para mostrar logos en el catálogo |

```tsx
// Barra superior
const marca = empresa?.nombre ?? "KAIRÓS";
{empresa?.logoUrl
  ? <img src={apiUrl + empresa.logoUrl} alt={empresa.nombre} height={32} />
  : <span className="logo-texto">{marca}</span>}
```

Sugerencia: una página **"Mi empresa"** solo para el admin, con el formulario de datos y la carga del logo con vista previa.

## 2. Panel de inicio (SCRUM-106)

`GET /api/panel`: admin, vendedor y superadmin. El cliente recibe 403. Es la pantalla ideal para la **página de inicio** de los empleados.

```json
{
  "rol": "ADMINISTRADOR",
  "ventas": {
    "hoy":          { "cantidad": 3, "total": 3050000 },
    "ultimos7Dias": { "cantidad": 12, "total": 9800000 },
    "mes":          { "cantidad": 30, "total": 25100000 },
    "porDia": [ { "dia": "2026-11-12", "cantidad": 0, "total": 0 }, "... 7 días, del más viejo a hoy ..." ],
    "masVendidosDelMes": [ { "producto": "Galaxy A55", "unidades": 8, "total": 12000000 } ]
  },
  "productosBajoStock": 3,
  "reclamosAbiertos": 1,
  "empresasPorEstado": null
}
```

- **Administrador:** toda su empresa. **Vendedor:** solo sus ventas y el bajo stock de su sede.
- **Superadministrador:** `ventas`, `productosBajoStock` y `reclamosAbiertos` llegan en `null`, y `empresasPorEstado` trae `{"PENDIENTE": 2, "ACTIVA": 5, ...}` (todos los estados, aunque estén en 0).
- **Diseño sugerido:**
  - 4 tarjetas: ventas de hoy, total del mes, bajo stock (enlace a Inventario) y reclamos abiertos (enlace a Garantías);
  - una gráfica de barras con `porDia`;
  - una lista corta con los más vendidos.
  - Para el superadmin, tarjetas por estado, con las pendientes destacadas y enlazadas a "Aprobar empresas".

## 3. Catálogo más completo (SCRUM-107)

`GET /api/productos` y `GET /api/productos/{id}` traen además:

| Campo | Uso en pantalla |
|---|---|
| `imagenUrl` | Foto del producto (SCRUM-103); si es `null`, el recuadro actual |
| `empresa` | "Vendido por *TecnoStore*" |
| `logoEmpresaUrl` | Logo pequeño junto al nombre de la empresa (`null` si no tiene) |
| `disponible` | `false` → etiqueta "Agotado" y el producto en gris al final de la lista |

`disponible` es `true` si hay unidades en alguna sede **activa**. No se muestra la cantidad exacta.

## 4. Factura (SCRUM-104)

`GET /api/ventas/{id}/factura` trae empresa y NIT, sede, vendedor, cliente con cédula y la garantía de cada producto. El diseño sugerido está en SCRUM-104. Con la marca, el encabezado de la factura puede llevar el **logo de la empresa**.

## 5. Ideas de estilo para que no "se vea hecho por IA"

- **Pocos colores:** el azul de la marca para acciones principales y grises para el resto. Nada de degradados azul → violeta ni sombras grandes.
- **Etiquetas de estado** (Activa, Agotado, Vigente) **planas**, de esquinas poco redondeadas y con texto, no solo color.
- **Títulos sin subrayados de colores;** jerarquía con tamaño y peso de la letra.
- **Números alineados a la derecha** y con formato colombiano (`$ 1.500.000`); fechas `dd/mm/aaaa`.
- **Estados vacíos con mensaje útil** ("Aún no hay ventas este mes. Registra la primera") en lugar de una tabla vacía.
- **Imágenes reales** (foto del producto y logo de la empresa) en vez de recuadros con texto.
