package com.blog.service;

import com.blog.entity.FriendLink;

import java.util.List;

public interface FriendLinkService {
    List<FriendLink> listActive();
    List<FriendLink> listAll();
    FriendLink create(FriendLink friendLink);
    FriendLink update(FriendLink friendLink);
    void delete(Long id);
}
