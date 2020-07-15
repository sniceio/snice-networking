package io.snice.networking.app.impl;

import io.snice.generics.Generics;
import io.snice.networking.app.AppBundle;
import io.snice.networking.common.Connection;
import junit.framework.TestCase;
import org.junit.Test;

public class BasicAppBundleTest {

    @Test
    public void typeTest() {
        // AppBundle<Connection<String>, String> bundle = new BasicAppBundle<>();
        AppBundle<Blah, String> bundle = new BasicAppBundle<>();
        Class c = Generics.getTypeParameter(bundle.getClass(), Connection.class);
        System.out.println(c);
        // System.out.println(bundle.getConnectionType());
        // System.out.println(bundle.getType());
    }

    public interface Blah extends Connection<String> {

    }

}