package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy tất cả người dùng
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}