package com.increff.pos.entity;

/**
 * User roles for role-based access control
 * SUPERVISOR: Full access to all endpoints including upload and reports
 * OPERATOR: Restricted access - cannot access upload endpoints and reports
 */
public enum Role {
    SUPERVISOR, OPERATOR
} 