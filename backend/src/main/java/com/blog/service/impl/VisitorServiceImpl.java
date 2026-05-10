package com.blog.service.impl;

import com.blog.dto.VisitorStats;
import com.blog.entity.DailyVisit;
import com.blog.entity.Visitor;
import com.blog.mapper.DailyVisitMapper;
import com.blog.mapper.VisitorMapper;
import com.blog.service.VisitorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    private final VisitorMapper visitorMapper;
    private final DailyVisitMapper dailyVisitMapper;

    @Override
    public void record(Visitor visitor) {
        visitorMapper.insert(visitor);

        LocalDate today = LocalDate.now();
        DailyVisit daily = dailyVisitMapper.selectOne(new LambdaQueryWrapper<DailyVisit>()
                .eq(DailyVisit::getVisitDate, today));
        if (daily == null) {
            daily = new DailyVisit();
            daily.setVisitDate(today);
            daily.setPvCount(1);
            daily.setUvCount(1);
            dailyVisitMapper.insert(daily);
        } else {
            daily.setPvCount(daily.getPvCount() + 1);
            dailyVisitMapper.updateById(daily);
        }
    }

    @Override
    public VisitorStats getStats() {
        VisitorStats stats = new VisitorStats();
        stats.setTotalPv(visitorMapper.selectTotalPv());
        stats.setTotalUv(visitorMapper.selectTotalUv());
        stats.setTodayPv(visitorMapper.selectPvCountByDate(LocalDate.now()));
        stats.setTodayUv(visitorMapper.selectUvCountByDate(LocalDate.now()));
        return stats;
    }
}
