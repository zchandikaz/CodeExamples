package org.example.child.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * @author : chandika
 * @since : 2022-10-08(Sat) 15:14
 **/
public class CustomServletContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(CustomServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Child-War Initialized !");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
