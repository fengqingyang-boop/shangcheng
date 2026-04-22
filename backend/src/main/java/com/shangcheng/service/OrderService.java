package com.shangcheng.service;

import com.shangcheng.entity.Order;
import com.shangcheng.entity.Product;
import com.shangcheng.entity.User;
import com.shangcheng.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Transactional
    public Order createOrder(User user, Product product, Integer quantity) {
        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        int totalPrice = product.getPrice() * quantity;
        
        userService.deductPoints(user, totalPrice);
        
        productService.updateStock(product.getId(), product.getStock() - quantity);
        
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setProductName(product.getName());
        order.setProductImage(product.getImagePath());
        order.setPrice(totalPrice);
        order.setQuantity(quantity);
        
        return orderRepository.save(order);
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
