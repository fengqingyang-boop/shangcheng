package com.shangcheng.controller;

import com.shangcheng.entity.CartItem;
import com.shangcheng.entity.User;
import com.shangcheng.service.CartService;
import com.shangcheng.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        List<CartItem> items = cartService.getUserCart(user);
        
        List<Map<String, Object>> result = items.stream()
                .map(this::cartItemToMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        long count = cartService.getCartCount(user);
        
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @PostMapping
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Long productId = request.get("productId") != null 
                    ? ((Number) request.get("productId")).longValue() 
                    : null;
            Integer quantity = request.get("quantity") != null 
                    ? ((Number) request.get("quantity")).intValue() 
                    : 1;
            
            if (productId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "商品ID不能为空"));
            }
            
            CartItem item = cartService.addToCart(user, productId, quantity);
            
            return ResponseEntity.ok(cartItemToMap(item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Integer quantity = request.get("quantity") != null 
                    ? ((Number) request.get("quantity")).intValue() 
                    : 1;
            
            CartItem item = cartService.updateQuantity(user, id, quantity);
            
            return ResponseEntity.ok(cartItemToMap(item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            cartService.removeFromCart(user, id);
            
            return ResponseEntity.ok(Map.of("message", "已移除"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @DeleteMapping
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        cartService.clearCart(user);
        
        return ResponseEntity.ok(Map.of("message", "购物车已清空"));
    }
    
    private Map<String, Object> cartItemToMap(CartItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("quantity", item.getQuantity());
        map.put("createdAt", item.getCreatedAt());
        map.put("updatedAt", item.getUpdatedAt());
        
        if (item.getProduct() != null) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", item.getProduct().getId());
            productMap.put("name", item.getProduct().getName());
            productMap.put("description", item.getProduct().getDescription());
            productMap.put("price", item.getProduct().getPrice());
            productMap.put("imagePath", item.getProduct().getImagePath());
            productMap.put("stock", item.getProduct().getStock());
            productMap.put("status", item.getProduct().getStatus());
            map.put("product", productMap);
        }
        
        return map;
    }
}
