package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarImagenProductoUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase;
import com.kairos.Kairos_backend.application.port.out.AlmacenamientoArchivosPort;
import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort;
import com.kairos.Kairos_backend.application.port.out.DescargaImagenesPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.CodigoBarras;
import com.kairos.Kairos_backend.domain.model.ImagenProducto;
import com.kairos.Kairos_backend.domain.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ImagenProductoService implements GestionarImagenProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImagenProductoService.class);
    private static final String CARPETA = "productos/";

    private final GestionarProductosUseCase catalogo;
    private final ProductoRepositoryPort productoRepository;
    private final AlmacenamientoArchivosPort archivos;
    private final CatalogoExternoPort catalogoExterno;
    private final DescargaImagenesPort descargas;

    public ImagenProductoService(GestionarProductosUseCase catalogo,
                                 ProductoRepositoryPort productoRepository,
                                 AlmacenamientoArchivosPort archivos,
                                 CatalogoExternoPort catalogoExterno,
                                 DescargaImagenesPort descargas) {
        this.catalogo = catalogo;
        this.productoRepository = productoRepository;
        this.archivos = archivos;
        this.catalogoExterno = catalogoExterno;
        this.descargas = descargas;
    }

    @Override
    public ImagenProducto ver(Long idProducto) {
        // Misma regla que el catálogo: un producto de una empresa no activa no se ve
        Producto producto = catalogo.obtener(idProducto);
        return Optional.ofNullable(producto.getImagen())
                .flatMap(archivos::leer)
                .map(ImagenProducto::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto " + idProducto + " no tiene imagen"));
    }

    @Override
    public Producto subir(Long idEmpresa, Long idProducto, byte[] contenido) {
        Producto producto = obtenerDeEmpresa(idEmpresa, idProducto);
        return guardarImagen(producto, ImagenProducto.desde(contenido));   // el dominio valida tipo y tamaño
    }

    @Override
    public Producto usarImagenDelCodigoBarras(Long idEmpresa, Long idProducto, String codigoBarras) {
        Producto producto = obtenerDeEmpresa(idEmpresa, idProducto);
        CodigoBarras codigo = new CodigoBarras(codigoBarras);
        var datos = catalogoExterno.buscarPorCodigoBarras(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un producto con el código " + codigo.valor()));
        // Muchas de esas direcciones ya no existen o no son fotos: se usa la primera que sirva
        for (String url : datos.imagenes()) {
            Optional<ImagenProducto> imagen = descargas.descargar(url).flatMap(ImagenProductoService::validar);
            if (imagen.isPresent()) {
                return guardarImagen(producto, imagen.get());
            }
        }
        throw new RecursoNoEncontradoException(
                "El código " + codigo.valor() + " no trae una foto que se pueda usar; súbala a mano");
    }

    @Override
    public void quitar(Long idEmpresa, Long idProducto) {
        Producto producto = obtenerDeEmpresa(idEmpresa, idProducto);
        String anterior = producto.quitarImagen();
        if (anterior != null) {
            productoRepository.guardar(producto);
            archivos.borrar(anterior);
        }
    }

    /**
     * Primero guarda el archivo nuevo y después la base de datos. Si la base falla, se borra el
     * archivo nuevo para no dejar basura; la foto anterior se borra solo cuando todo salió bien.
     */
    private Producto guardarImagen(Producto producto, ImagenProducto imagen) {
        String ruta = CARPETA + producto.getId() + "-" + UUID.randomUUID() + "." + imagen.tipo().getExtension();
        archivos.guardar(ruta, imagen.contenido());
        String anterior = producto.cambiarImagen(ruta);
        Producto guardado;
        try {
            guardado = productoRepository.guardar(producto);
        } catch (RuntimeException e) {
            archivos.borrar(ruta);
            throw e;
        }
        if (anterior != null) {
            archivos.borrar(anterior);
        }
        log.info("Imagen del producto {} guardada en {}", producto.getId(), ruta);
        return guardado;
    }

    private static Optional<ImagenProducto> validar(byte[] contenido) {
        try {
            return Optional.of(ImagenProducto.desde(contenido));
        } catch (ReglaNegocioException e) {
            return Optional.empty();
        }
    }

    private Producto obtenerDeEmpresa(Long idEmpresa, Long id) {
        return ProductosDeEmpresa.obtener(productoRepository, idEmpresa, id);
    }
}
