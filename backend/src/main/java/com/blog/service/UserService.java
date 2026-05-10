package com.blog.service;

import com.blog.entity.User;

public interface UserService {
    User getByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword);
}
