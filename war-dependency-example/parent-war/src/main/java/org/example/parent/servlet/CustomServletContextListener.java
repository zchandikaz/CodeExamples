package org.example.parent.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.example.ProxyClassLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author : chandika
 * @since : 2022-10-08(Sat) 15:14
 **/
public class CustomServletContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(CustomServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Parent-War Initialized !");
        try {
            ProxyClassLoader.setForeignClassLoader(this.getClass().getClassLoader());
            LOGGER.log(Level.INFO, "Classloader registration success!", new Object[]{  });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "ClassLoader registration failed!");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
