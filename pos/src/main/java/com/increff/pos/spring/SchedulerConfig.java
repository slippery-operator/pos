package com.increff.pos.spring;

import com.increff.pos.flow.ReportFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for scheduled tasks
 * Handles automatic daily sales calculations
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private ReportFlow reportFlow;

    // The scheduler is now enabled and will run the calculateDailySales method
    // in ReportFlow every day at 23:59 UTC
}
