package com.blog.controller;

import com.blog.dto.ChangePasswordRequest;
import com.blog.service.UserService;
import com.blog.utils.Result;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/account")
@RequiredArgsConstructor
public class AdminAccountController {

    private final UserService userService;

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
