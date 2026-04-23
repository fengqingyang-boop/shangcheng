package com.shangcheng.service;

import com.shangcheng.entity.CartItem;
import com.shangcheng.entity.Product;
import com.shangcheng.entity.User;
import com.shangcheng.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductService productService;
    
    public List<CartItem> getUserCart(User user) {
        return cartItemRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public long getCartCount(User user) {
        return cartItemRepository.countByUser(user);
    }
    
    @Transactional
    public CartItem addToCart(User user, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }
        
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        if (!Product.STATUS_ACTIVE.equals(product.getStatus())) {
            throw new RuntimeException("商品已下架，无法加入购物车");
        }
        
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProductId(user, productId);
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("购物车数量已超过库存，当前库存: " + product.getStock());
            }
            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            if (quantity > product.getStock()) {
                throw new RuntimeException("数量超过库存，当前库存: " + product.getStock());
            }
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            return cartItemRepository.save(newItem);
        }
    }
    
    @Transactional
    public CartItem updateQuantity(User user, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此购物车项");
        }
        
        Product product = item.getProduct();
        if (quantity > product.getStock()) {
            throw new RuntimeException("数量超过库存，当前库存: " + product.getStock());
        }
        
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }
    
    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此购物车项");
        }
        
        cartItemRepository.delete(item);
    }
    
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}
