package com.fastorder.catalog.controller;

import com.fastorder.catalog.domain.dto.ComercioRequest;
import com.fastorder.catalog.domain.dto.ComercioResponse;
import com.fastorder.catalog.domain.dto.ProductoRequest;
import com.fastorder.catalog.domain.dto.ProductoResponse;
import com.fastorder.catalog.domain.enums.Categoria;
import com.fastorder.catalog.service.CatalogService;
import com.fastorder.common.domain.AppConstants;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/comercios")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public Page<ComercioResponse> getComercios(
            @RequestParam(required = false) Categoria categoria,
            Pageable pageable) {
        return catalogService.getComerciosActivos(categoria, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ComercioResponse createComercio(@Valid @RequestBody ComercioRequest request) {
        return catalogService.createComercio(request);
    }

    @GetMapping("/{id}/productos")
    public Page<ProductoResponse> getProductosByComercio(
            @PathVariable Long id,
            Pageable pageable) {
        return catalogService.getProductosByComercio(id, pageable);
    }

    @PostMapping("/{id}/productos")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse createProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return catalogService.createProducto(id, request);
    }
}
