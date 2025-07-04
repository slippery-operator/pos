package com.increff.pos.spring;

import com.increff.pos.dto.ReportDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration for scheduled tasks
 * Handles automatic daily sales calculations
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private ReportDto reportDto;

    // All scheduled methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
}
