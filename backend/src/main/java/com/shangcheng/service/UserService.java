package com.shangcheng.service;

import com.shangcheng.dto.RegisterRequest;
import com.shangcheng.entity.User;
import com.shangcheng.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPoints(100);
        user.setRole("USER");
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User updatePoints(Long userId, Integer points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPoints(points);
        return userRepository.save(user);
    }
    
    public User deductPoints(User user, Integer points) {
        if (user.getPoints() < points) {
            throw new RuntimeException("积分不足");
        }
        user.setPoints(user.getPoints() - points);
        return userRepository.save(user);
    }
}
