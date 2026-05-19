package com.housekeeping.controller;

import com.housekeeping.common.Result;
import com.housekeeping.entity.User;
import com.housekeeping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        try {
            User created = userService.createUser(user);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable String id) {
        User user = userService.getUser(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @PutMapping
    public Result<User> updateUser(@RequestBody User user) {
        try {
            User updated = userService.updateUser(user);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
