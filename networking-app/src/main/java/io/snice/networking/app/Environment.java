package io.snice.networking.app;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Environment<T, C extends NetworkAppConfig> {

    /**
     * Obtain the loaded configuration.
     */
    C getConfig();

}
