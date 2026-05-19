package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.CatalogRequestDTO;
import com.project.retailproject.dto.CatalogResponseDTO;
import com.project.retailproject.service.CatalogService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<CatalogResponseDTO>> addCatalog(
            @Valid @RequestBody CatalogRequestDTO dto) {
        CatalogResponseDTO data = catalogService.insertCatalog(dto);
        return ResponseEntity.ok(ApiResponse.success("Catalog created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CatalogResponseDTO>>> getAllCatalogs() {
        List<CatalogResponseDTO> data = catalogService.getAllCatalogs();
        return ResponseEntity.ok(ApiResponse.success("Catalogs retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogResponseDTO>> getCatalogById(@PathVariable Long id) {
        CatalogResponseDTO data = catalogService.getCatalogById(id);
        return ResponseEntity.ok(ApiResponse.success("Catalog retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogResponseDTO>> updateCatalog(
            @PathVariable Long id,
            @Valid @RequestBody CatalogRequestDTO dto) {
        CatalogResponseDTO data = catalogService.updateCatalog(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Catalog updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCatalog(@PathVariable Long id) {
        catalogService.deleteCatalog(id);
        return ResponseEntity.ok(ApiResponse.success("Catalog deactivated successfully", null));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<CatalogResponseDTO>>> getCatalogsByProduct(
            @PathVariable Long productId) {
        List<CatalogResponseDTO> data = catalogService.getCatalogsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success("Catalogs retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<CatalogResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CatalogResponseDTO> data = catalogService.getAllCatalogsWithPagination(page, size);
        return ResponseEntity.ok(ApiResponse.success("Catalogs retrieved successfully", data));
    }
}