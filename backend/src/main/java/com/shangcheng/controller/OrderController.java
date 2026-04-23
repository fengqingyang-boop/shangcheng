package com.shangcheng.controller;

import com.shangcheng.dto.OrderRequest;
import com.shangcheng.entity.Order;
import com.shangcheng.entity.Product;
import com.shangcheng.entity.User;
import com.shangcheng.service.OrderService;
import com.shangcheng.service.ProductService;
import com.shangcheng.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Product product = productService.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            
            Order order = orderService.createOrder(user, product, request.getQuantity());
            
            return ResponseEntity.ok(orderToMap(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Order order = orderService.payOrder(id, user);
            
            return ResponseEntity.ok(orderToMap(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Order order = orderService.cancelOrder(id, user);
            
            return ResponseEntity.ok(orderToMap(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/refund")
    public ResponseEntity<?> refundOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Order order = orderService.refundOrder(id, user);
            
            return ResponseEntity.ok(orderToMap(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        List<Order> orders = orderService.getUserOrders(user);
        
        List<Map<String, Object>> result = orders.stream()
                .map(this::orderToMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderService.findAll();
        
        List<Map<String, Object>> result = orders.stream()
                .map(this::orderToMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        if (order.getUser() != null) {
            map.put("userId", order.getUser().getId());
            map.put("username", order.getUser().getUsername());
        }
        map.put("productId", order.getProduct() != null ? order.getProduct().getId() : null);
        map.put("productName", order.getProductName());
        map.put("productImage", order.getProductImage());
        map.put("price", order.getPrice());
        map.put("quantity", order.getQuantity());
        map.put("status", order.getStatus());
        map.put("createdAt", order.getCreatedAt());
        map.put("paidAt", order.getPaidAt());
        map.put("cancelledAt", order.getCancelledAt());
        map.put("refundedAt", order.getRefundedAt());
        return map;
    }
}
