package com.shangcheng.repository;

import com.shangcheng.entity.Order;
import com.shangcheng.entity.Product;
import com.shangcheng.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    void deleteByProduct(Product product);
    List<Order> findByProduct(Product product);
}
