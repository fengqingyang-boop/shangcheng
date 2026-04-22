package com.shangcheng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shangcheng.dto.ProductRequest;
import com.shangcheng.entity.Product;
import com.shangcheng.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(
            @RequestParam("product") String productJson,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            ProductRequest request = objectMapper.readValue(productJson, ProductRequest.class);
            Product product = productService.createProduct(request, imageFile);
            return ResponseEntity.ok(product);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "参数解析失败"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("product") String productJson,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            ProductRequest request = objectMapper.readValue(productJson, ProductRequest.class);
            Product product = productService.updateProduct(id, request, imageFile);
            return ResponseEntity.ok(product);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "参数解析失败"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
