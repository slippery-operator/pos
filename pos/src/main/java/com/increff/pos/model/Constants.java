package com.increff.pos.model;

public class Constants {

    public static final int DB_INITIAL_SIZE = 2;
    public static final boolean DB_DEFAULT_AUTO_COMMIT = false;
    public static final int DB_MIN_IDLE = 2;
    public static final String DB_VALIDATION_QUERY = "Select 1";
    public static final boolean DB_TEST_WHILE_IDLE = true;
    public static final long DB_EVICTION_RUN_INTERVAL_MS = 10 * 60 * 100L;
    public static final String PACKAGE_POJO_LOCATION = "com.increff.pos.entity";
    public static final String PACKAGE_CONTROLLER = "com.increff.pos.controller";
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_ROLE = "userRole";
    public static final String SESSION_LAST_CHECKED_TIME = "lastCheckedTime";
    public static final long SESSION_REVALIDATION_INTERVAL_MS = 300_000; // 5 minutes in milliseconds
    // Optional: Add comments if needed
}
