package com.shangcheng.service;

import com.shangcheng.entity.Order;
import com.shangcheng.entity.Product;
import com.shangcheng.entity.User;
import com.shangcheng.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    @Transactional
    public Order createPendingOrder(User user, Product product, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("购买数量必须大于0");
        }
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足，当前库存: " + product.getStock());
        }
        
        if (!Product.STATUS_ACTIVE.equals(product.getStatus())) {
            throw new RuntimeException("商品已下架，无法购买");
        }
        
        int totalPrice = product.getPrice() * quantity;
        
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setProductName(product.getName());
        order.setProductImage(product.getImagePath());
        order.setPrice(totalPrice);
        order.setQuantity(quantity);
        order.setStatus(Order.STATUS_PENDING_PAYMENT);
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order createOrder(User user, Product product, Integer quantity) {
        Order order = createPendingOrder(user, product, quantity);
        return payOrder(order.getId(), user);
    }
    
    @Transactional
    public Order payOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (!Order.STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许支付");
        }
        
        Product product = order.getProduct();
        if (product == null) {
            throw new RuntimeException("商品信息不存在");
        }
        
        if (product.getStock() < order.getQuantity()) {
            throw new RuntimeException("商品库存不足，当前库存: " + product.getStock());
        }
        
        int totalPrice = order.getPrice();
        
        if (user.getPoints() < totalPrice) {
            throw new RuntimeException("积分不足，需要: " + totalPrice + " 积分");
        }
        
        userService.deductPoints(user, totalPrice);
        
        productService.updateStock(product.getId(), product.getStock() - order.getQuantity());
        
        order.setStatus(Order.STATUS_PAID);
        order.setPaidAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order cancelOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (!Order.STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new RuntimeException("只有待支付订单可以取消");
        }
        
        order.setStatus(Order.STATUS_CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order refundOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (!Order.STATUS_PAID.equals(order.getStatus())) {
            throw new RuntimeException("只有已支付订单可以退款");
        }
        
        userService.updatePoints(user.getId(), user.getPoints() + order.getPrice());
        
        Product product = order.getProduct();
        if (product != null) {
            productService.updateStock(product.getId(), product.getStock() + order.getQuantity());
        }
        
        order.setStatus(Order.STATUS_REFUNDED);
        order.setRefundedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
