package com.fastorder.catalog.service;

import com.fastorder.catalog.domain.dto.ComercioRequest;
import com.fastorder.catalog.domain.dto.ComercioResponse;
import com.fastorder.catalog.domain.dto.ProductoRequest;
import com.fastorder.catalog.domain.dto.ProductoResponse;
import com.fastorder.catalog.domain.entity.Comercio;
import com.fastorder.catalog.domain.entity.Producto;
import com.fastorder.catalog.domain.enums.Categoria;
import com.fastorder.catalog.repository.ComercioRepository;
import com.fastorder.catalog.repository.ProductoRepository;
import com.fastorder.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    private final ComercioRepository comercioRepository;
    private final ProductoRepository productoRepository;
    private final com.fastorder.common.audit.AuditService auditService;

    public CatalogService(ComercioRepository comercioRepository, ProductoRepository productoRepository, com.fastorder.common.audit.AuditService auditService) {
        this.comercioRepository = comercioRepository;
        this.productoRepository = productoRepository;
        this.auditService = auditService;
    }

    public Page<ComercioResponse> getComerciosActivos(Categoria categoria, Pageable pageable) {
        Page<Comercio> comercios;
        if (categoria != null) {
            comercios = comercioRepository.findByAbiertoTrueAndCategoria(categoria, pageable);
        } else {
            comercios = comercioRepository.findByAbiertoTrue(pageable);
        }
        return comercios.map(this::mapToComercioResponse);
    }

    public ComercioResponse createComercio(ComercioRequest request, com.fastorder.common.security.AuthenticatedUser user) {
        Comercio comercio = Comercio.builder()
                .nombre(request.nombre())
                .categoria(request.categoria())
                .direccion(request.direccion())
                .abierto(request.abierto())
                .build();
        comercio = comercioRepository.save(comercio);
        
        auditService.registrar(user.id(), user.email(), "COMERCIO", comercio.getId(), "CREAR_COMERCIO", "Comercio creado: " + comercio.getNombre());
        
        return mapToComercioResponse(comercio);
    }

    public Page<ProductoResponse> getProductosByComercio(Long comercioId, Pageable pageable) {
        if (!comercioRepository.existsById(comercioId)) {
            throw new ResourceNotFoundException("Comercio no encontrado");
        }
        Page<Producto> productos = productoRepository.findByComercioIdAndDisponibleTrue(comercioId, pageable);
        return productos.map(this::mapToProductoResponse);
    }

    public ProductoResponse createProducto(Long comercioId, ProductoRequest request, com.fastorder.common.security.AuthenticatedUser user) {
        Comercio comercio = comercioRepository.findById(comercioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comercio no encontrado"));

        Producto producto = Producto.builder()
                .comercio(comercio)
                .nombre(request.nombre())
                .precio(request.precio())
                .stock(request.stock())
                .disponible(request.disponible())
                .build();

        producto = productoRepository.save(producto);
        
        auditService.registrar(user.id(), user.email(), "PRODUCTO", producto.getId(), "CREAR_PRODUCTO", "Producto creado: " + producto.getNombre() + " en comercio " + comercio.getId());
        
        return mapToProductoResponse(producto);
    }

    private ComercioResponse mapToComercioResponse(Comercio c) {
        return new ComercioResponse(c.getId(), c.getNombre(), c.getCategoria(), c.getDireccion(), c.getAbierto());
    }

    private ProductoResponse mapToProductoResponse(Producto p) {
        return new ProductoResponse(p.getId(), p.getComercio().getId(), p.getNombre(), p.getPrecio(), p.getStock(), p.getDisponible());
    }
}
