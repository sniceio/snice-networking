package io.snice.networking.app;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Environment<T extends NetworkAppConfig> {

    /**
     * Obtain the loaded configuration.
     */
    T getConfig();

}
