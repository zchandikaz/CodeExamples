package org.example;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappClassLoaderBase;

import java.util.logging.Logger;

/**
 * @author : chandika
 * @since : 2022-10-08(Sat) 14:43
 **/
public class ContextClassLoader extends WebappClassLoaderBase {

    public ContextClassLoader(ClassLoader parent) {
        super(new ProxyClassLoader(parent));
    }

    @Override
    public ClassLoader copyWithoutTransformers() {
        ContextClassLoader result = new ContextClassLoader(getParent());

        super.copyStateWithoutTransformers(result);

        try {
            result.start();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }
}
