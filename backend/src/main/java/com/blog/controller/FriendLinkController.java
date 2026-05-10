package com.blog.controller;

import com.blog.entity.FriendLink;
import com.blog.service.FriendLinkService;
import com.blog.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    @GetMapping("/friend-links")
    public Result<List<FriendLink>> listActive() {
        return Result.success(friendLinkService.listActive());
    }

    @GetMapping("/admin/friend-links")
    public Result<List<FriendLink>> listAll() {
        return Result.success(friendLinkService.listAll());
    }

    @PostMapping("/admin/friend-links")
    public Result<FriendLink> create(@RequestBody FriendLink friendLink) {
        return Result.success(friendLinkService.create(friendLink));
    }

    @PutMapping("/admin/friend-links/{id}")
    public Result<FriendLink> update(@PathVariable Long id, @RequestBody FriendLink friendLink) {
        friendLink.setId(id);
        return Result.success(friendLinkService.update(friendLink));
    }

    @DeleteMapping("/admin/friend-links/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        friendLinkService.delete(id);
        return Result.success();
    }
}
