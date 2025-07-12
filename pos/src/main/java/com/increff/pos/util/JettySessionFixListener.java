//package com.increff.pos.util;
//
//import org.eclipse.jetty.server.session.SessionHandler;
//
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//import javax.servlet.annotation.WebListener;
//
//@WebListener
//public class JettySessionFixListener implements ServletContextListener {
//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        Object handler = sce.getServletContext().getAttribute("org.eclipse.jetty.server.session.SessionHandler");
//
//        if (handler instanceof SessionHandler) {
//            SessionHandler sessionHandler = (SessionHandler) handler;
//
//            // ✅ Disable appending .node0 etc. to session IDs
//            sessionHandler.setNodeIdInSessionId(false);
//
//            System.out.println("✅ Jetty node ID suffix (.node0) disabled on JSESSIONID");
//        } else {
//            System.out.println("❌ SessionHandler not found or wrong type: " + (handler != null ? handler.getClass() : "null"));
//        }
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//    }
//}
