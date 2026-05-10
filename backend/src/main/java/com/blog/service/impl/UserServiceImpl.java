package com.blog.service.impl;

import com.blog.entity.User;
import com.blog.exception.BlogException;
import com.blog.exception.ErrorCode;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getByUsername(username);
        if (user == null) {
            throw new BlogException(ErrorCode.INVALID_CREDENTIALS.getCode(), ErrorCode.INVALID_CREDENTIALS.getMessage());
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BlogException(ErrorCode.OLD_PASSWORD_INCORRECT.getCode(), ErrorCode.OLD_PASSWORD_INCORRECT.getMessage());
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
