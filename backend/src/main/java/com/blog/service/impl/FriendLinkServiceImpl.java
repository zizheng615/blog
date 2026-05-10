package com.blog.service.impl;

import com.blog.entity.FriendLink;
import com.blog.mapper.FriendLinkMapper;
import com.blog.service.FriendLinkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendLinkServiceImpl implements FriendLinkService {

    private final FriendLinkMapper friendLinkMapper;

    @Override
    public List<FriendLink> listActive() {
        return friendLinkMapper.selectList(new LambdaQueryWrapper<FriendLink>()
                .eq(FriendLink::getIsActive, true)
                .orderByAsc(FriendLink::getSortOrder));
    }

    @Override
    public List<FriendLink> listAll() {
        return friendLinkMapper.selectList(new LambdaQueryWrapper<FriendLink>()
                .orderByAsc(FriendLink::getSortOrder));
    }

    @Override
    public FriendLink create(FriendLink friendLink) {
        friendLinkMapper.insert(friendLink);
        return friendLink;
    }

    @Override
    public FriendLink update(FriendLink friendLink) {
        friendLinkMapper.updateById(friendLink);
        return friendLink;
    }

    @Override
    public void delete(Long id) {
        friendLinkMapper.deleteById(id);
    }
}
