package com.housekeeping.service;

import com.housekeeping.entity.User;
import com.housekeeping.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private InMemoryStorage storage;

    public User createUser(User user) {
        user.setId(UUID.randomUUID().toString().replace("-", ""));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        storage.saveUser(user);
        return user;
    }

    public User getUser(String id) {
        return storage.getUser(id);
    }

    public List<User> getAllUsers() {
        return storage.getAllUsers();
    }

    public User updateUser(User user) {
        User existing = storage.getUser(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setCreatedAt(existing.getCreatedAt());
        user.setUpdatedAt(LocalDateTime.now());
        storage.saveUser(user);
        return user;
    }
}
