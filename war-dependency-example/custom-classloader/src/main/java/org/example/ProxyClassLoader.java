package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;

/**
 * @author : chandika
 * @since : 2022-10-08(Sat) 14:56
 **/
public class ProxyClassLoader extends ClassLoader{
    private ClassLoader actualClassLoader;
    private static ClassLoader foreignClassLoader;

    public ProxyClassLoader(ClassLoader actualClassLoader) {
        this.actualClassLoader = actualClassLoader;
    }

    @Override
    public String getName() {
        return actualClassLoader == null ? null : actualClassLoader.getName();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return (foreignClassLoader == null ? actualClassLoader : foreignClassLoader).loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        return actualClassLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return actualClassLoader.getResources(name);
    }

    @Override
    public Stream<URL> resources(String name) {
        return actualClassLoader.resources(name);
    }

    public static URL getSystemResource(String name) {
        return ClassLoader.getSystemResource(name);
    }

    public static Enumeration<URL> getSystemResources(String name) throws IOException {
        return ClassLoader.getSystemResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return actualClassLoader.getResourceAsStream(name);
    }

    public static InputStream getSystemResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    }

    public static ClassLoader getPlatformClassLoader() {
        return ClassLoader.getPlatformClassLoader();
    }

    public static ClassLoader getSystemClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        actualClassLoader.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        actualClassLoader.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        actualClassLoader.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        actualClassLoader.clearAssertionStatus();
    }

    public ClassLoader getActualClassLoader() {
        return actualClassLoader;
    }

    public void setActualClassLoader(ClassLoader actualClassLoader) {
        this.actualClassLoader = actualClassLoader;
    }

    public static ClassLoader getForeignClassLoader() {
        return foreignClassLoader;
    }

    public static void setForeignClassLoader(ClassLoader foreignClassLoader) {
        ProxyClassLoader.foreignClassLoader = foreignClassLoader;
    }

    public static boolean isReady(){
        return foreignClassLoader!=null;
    }
}
