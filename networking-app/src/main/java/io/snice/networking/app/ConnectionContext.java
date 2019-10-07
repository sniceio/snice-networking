package io.snice.networking.app;

import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ConnectionContext<C extends Connection, T, R> extends Predicate<ConnectionId> {

    boolean isDrop();


    MessagePipe<C, T, ?> match(C connection, T data);

    interface Builder<C extends Connection, T, R> {
        /**
         * If you decide to accept the incoming connection, then you must also specify what
         * operations to perform on the incoming data.
         */
        void accept(Consumer<ConfigurationBuilder<C, T, R>> builder);

        /**
         * Do not accept the incoming connection and as such, we'll just disconnect it
         * right away without telling the far end anything...
         */
        void drop();

        void drop(Function<C, T> f);
    }

    interface ConfigurationBuilder<C extends Connection, T, R> {

        ConfigurationBuilder<C, T, R> withDefaultStatisticsModule();

        MessageProcessingBuilder<C, T, R> match(Predicate<T> filter);
    }

    interface MessageProcessingBuilder<C extends Connection, T, R> {

        MessageProcessingBuilder<C, T, R> consume(Consumer<R> consumer);

        MessageProcessingBuilder<C, T, R> consume(BiConsumer<C, R> consumer);

        <NEW_R> MessageProcessingBuilder<C, T, NEW_R> map(Function<? super R, ? extends NEW_R> f);

        <NEW_R> MessageProcessingBuilder<C, T, NEW_R> map(BiFunction<C, ? super R, ? extends NEW_R> f);
    }
}
