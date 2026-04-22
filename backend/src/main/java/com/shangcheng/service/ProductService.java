package com.shangcheng.service;

import com.shangcheng.dto.ProductRequest;
import com.shangcheng.entity.Order;
import com.shangcheng.entity.Product;
import com.shangcheng.repository.OrderRepository;
import com.shangcheng.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    public Product createProduct(ProductRequest request, MultipartFile imageFile) throws IOException {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 1);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            product.setImagePath(imagePath);
        }
        
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, ProductRequest request, MultipartFile imageFile) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        
        if (imageFile != null && !imageFile.isEmpty()) {
            if (product.getImagePath() != null) {
                deleteImage(product.getImagePath());
            }
            String imagePath = saveImage(imageFile);
            product.setImagePath(imagePath);
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        List<Order> orders = orderRepository.findByProduct(product);
        if (!orders.isEmpty()) {
            throw new RuntimeException("该商品已被购买，无法删除");
        }
        
        if (product.getImagePath() != null) {
            deleteImage(product.getImagePath());
        }
        
        productRepository.deleteById(id);
    }
    
    public Product updateStock(Long id, Integer stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setStock(stock);
        return productRepository.save(product);
    }
    
    private String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return "/uploads/" + filename;
    }
    
    private void deleteImage(String imagePath) {
        try {
            if (imagePath.startsWith("/uploads/")) {
                String filename = imagePath.substring("/uploads/".length());
                Path filePath = Paths.get(uploadDir, filename);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
