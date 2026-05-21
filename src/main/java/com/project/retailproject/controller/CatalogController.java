package com.project.retailproject.controller;

import com.project.retailproject.dto.CatalogRequestDTO;
import com.project.retailproject.dto.CatalogResponseDTO;
import com.project.retailproject.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogs")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @PostMapping
    public ResponseEntity<CatalogResponseDTO> createCatalog(@RequestBody CatalogRequestDTO dto) {
        return ResponseEntity.ok(catalogService.insertCatalog(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogResponseDTO> updateCatalog(
            @PathVariable Long id, @RequestBody CatalogRequestDTO dto) {
        return ResponseEntity.ok(catalogService.updateCatalog(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalog(@PathVariable Long id) {
        catalogService.deleteCatalog(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogResponseDTO> getCatalog(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getCatalogById(id));
    }

    @GetMapping
    public ResponseEntity<List<CatalogResponseDTO>> getAllCatalogs() {
        return ResponseEntity.ok(catalogService.getAllCatalogs());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CatalogResponseDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(catalogService.getCatalogsByProductId(productId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<CatalogResponseDTO>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.getAllCatalogsWithPagination(page, size));
    }
}