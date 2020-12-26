package io.snice.networking.examples.vplmn;

public interface Error {

    static Error of(final String msg) {
        return () -> msg;
    }

    String getMessage();
}
