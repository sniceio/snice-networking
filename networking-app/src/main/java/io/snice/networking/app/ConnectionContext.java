package io.snice.networking.app;

import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.Optional;
import java.util.function.*;

public interface ConnectionContext<C extends Connection<T>, T> extends Predicate<ConnectionId> {

    Predicate<ConnectionId> getPredicate();

    Optional<Function<C, T>> getDropFunction();

    Optional<Consumer<C>> getSaveAction();

    default boolean isDrop() {
        return getDropFunction().isPresent();
    }

    MessagePipe<C, T, ?> match(C connection, T data);

    <U extends Object> MessagePipe<C, U, ?> matchEvent(C connection, U event);

    interface Builder<C extends Connection<T>, T, R> {
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

        /**
         * @param f
         * @return
         */
        Builder<C, T, R> save(Consumer<C> f);

        // TODO: wrong. It's not a function, it's just a consumer!
        void drop(Function<C, T> f);
    }

    interface ConfigurationBuilder<C extends Connection<T>, T, R> {

        ConfigurationBuilder<C, T, R> withDefaultStatisticsModule();

        MessageProcessingBuilder<C, T, R> match(Predicate<T> filter);

        <T2, R2> EventProcessingBuilder<C, T, T2, R2> matchEvent(Predicate<T2> filter);

        void withPipe(MessagePipe<C, T, ?> pipe);

        void withPipe(SingleMessagePipe<T, ?> pipe);
    }

    interface EventProcessingBuilder<C extends Connection<T2>, T2, T, R> {

        // <NEW_R> EventProcessingBuilder<T, NEW_R> withPipe(MessagePipe<C, ? super R, ? extends NEW_R> f);

        EventProcessingBuilder<C, T2, T, R> consume(Consumer<R> consumer);

        EventProcessingBuilder<C, T2, T, R> consume(BiConsumer<C, R> consumer);

        <NEW_R> EventProcessingBuilder<C, T2, T, NEW_R> map(Function<? super R, ? extends NEW_R> f);

        // <NEW_R> EventProcessingBuilder<T, NEW_R> map(BiFunction<C, ? super R, ? extends NEW_R> f);
    }

    interface MessageProcessingBuilder<C extends Connection<T>, T, R> {

        <NEW_R> MessageProcessingBuilder<C, T, NEW_R> withPipe(MessagePipe<C, ? super R, ? extends NEW_R> f);

        MessageProcessingBuilder<C, T, R> consume(Consumer<R> consumer);

        MessageProcessingBuilder<C, T, R> consume(BiConsumer<C, R> consumer);

        <NEW_R> MessageProcessingBuilder<C, T, NEW_R> map(Function<? super R, ? extends NEW_R> f);

        <NEW_R> MessageProcessingBuilder<C, T, NEW_R> map(BiFunction<C, ? super R, ? extends NEW_R> f);

    }
}
