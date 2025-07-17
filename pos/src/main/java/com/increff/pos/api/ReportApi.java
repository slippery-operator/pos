package com.increff.pos.api;

import com.increff.pos.dao.ReportDao;
import com.increff.pos.entity.DaySalesPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional
public class ReportApi {

    @Autowired
    private ReportDao reportDao;

    public List<DaySalesPojo> getDaySalesByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return reportDao.getDaySalesByDateRange(startDate, endDate);
    }

    public void saveDaySales(DaySalesPojo daySales) {
        reportDao.insert(daySales);
    }

    public DaySalesPojo getDaySalesByDate(ZonedDateTime date) {
        return reportDao.getDaySalesByDate(date);
    }

    public void updateDaySales(DaySalesPojo daySales) {
        reportDao.update(daySales);
    }
} 