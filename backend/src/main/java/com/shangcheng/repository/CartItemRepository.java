package com.shangcheng.repository;

import com.shangcheng.entity.CartItem;
import com.shangcheng.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserOrderByCreatedAtDesc(User user);
    Optional<CartItem> findByUserAndProductId(User user, Long productId);
    void deleteByUser(User user);
    long countByUser(User user);
}
