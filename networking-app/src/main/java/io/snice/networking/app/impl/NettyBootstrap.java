package io.snice.networking.app.impl;

import io.snice.codecs.codec.SerializationFactory;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.ConnectionContext.MessageProcessingBuilder;
import io.snice.networking.app.MessagePipe;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.SingleMessagePipe;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class NettyBootstrap<K extends Connection<T>, T, C extends NetworkAppConfig> implements NetworkBootstrap<K, T, C> {

    private SerializationFactory<T> serializationFactory;

    private final C config;

    private final List<ConnectionCtxBuilder<K, T, ?>> rules = new ArrayList<>();

    public NettyBootstrap(final C config) {
        this.config = config;
    }

    public List<ConnectionContext> getConnectionContexts() {
        return rules.stream().map(ConnectionCtxBuilder::build).collect(Collectors.toList());
    }

    public SerializationFactory<T> getSerializationFactory() {
        return serializationFactory;
    }

    @Override
    public C getConfiguration() {
        return config;
    }

    @Override
    public void registerSerializationFactory(final SerializationFactory<T> serializationFactory) {
        assertNotNull(serializationFactory, "The serializationFactory cannot be null");
        this.serializationFactory = serializationFactory;
    }

    @Override
    public ConnectionContext.Builder<K, T, T> onConnection(final Predicate<ConnectionId> condition) {
        assertNotNull(condition, "The condition cannot be null");
        final ConnectionCtxBuilder<K, T, T> builder = new ConnectionCtxBuilder<>(condition);
        rules.add(builder);
        return builder;
    }

    private static class ConnectionCtxBuilder<K extends Connection<T>, T, R> implements ConnectionContext.Builder<K, T, R> {

        private final Predicate<ConnectionId> condition;
        private Function<K, T> dropFunction;
        private Consumer<ConnectionContext.ConfigurationBuilder<K, T, R>> confBuilderConsumer;

        private ConnectionCtxBuilder(final Predicate<ConnectionId> condition) {
            this.condition = condition;
        }

        public ConnectionContext<K, T> build() {
            System.out.println("Building the connection context");
            final List<MessagePipe<K, T, ?>> rules;
            if (confBuilderConsumer != null) {
                final ConfBuilder b = new ConfBuilder();
                confBuilderConsumer.accept(b);
                rules = b.getRules();
                System.out.println("The rules are: " + rules);
            } else {
                rules = List.of();
            }

            return new NettyConnectionContext<K, T>(condition, dropFunction, rules);
        }

        @Override
        public void accept(final Consumer<ConnectionContext.ConfigurationBuilder<K, T, R>> consumer) {
            assertArgument(dropFunction == null, "You have already marked this connection to be dropped, " +
                    "you cannot also accept the connection. You have to choose one or the other");
            assertNotNull(consumer, "The consumer cannot be null");
            confBuilderConsumer = consumer;
        }

        @Override
        public void drop() {
            drop(c -> null);
        }

        @Override
        public void drop(final Function<K, T> f) {
            assertArgument(confBuilderConsumer == null, "You have already marked this connection to be accepted, " +
                    "you cannot also drop the connection. You have to choose one or the other");
            assertArgument(dropFunction == null, "You cannot specify to drop the connection twice");
            assertNotNull(f, "The drop function cannot be null");
            dropFunction = f;
        }

        private class ConfBuilder implements ConnectionContext.ConfigurationBuilder<K, T, R> {

            private final List<MessageProcessingBuilderImpl<K, T, T>> rules = new ArrayList<>();

            @Override
            public ConnectionContext.ConfigurationBuilder<K, T, R> withDefaultStatisticsModule() {
                return this;
            }

            private List<MessagePipe<K, T, ?>> getRules() {
                return rules.stream().map(MessageProcessingBuilderImpl::getFinalPipe).collect(Collectors.toList());
            }

            @Override
            public MessageProcessingBuilder<K, T, R> match(final Predicate<T> filter) {
                final BiPredicate<K, T> filter2 = (c, t) -> filter.test(t);
                final MessagePipe<K, T, T> initialPipe = MessagePipe.match(filter2);
                final MessageProcessingBuilderImpl builder = new MessageProcessingBuilderImpl(initialPipe);
                rules.add(builder);
                return builder;
            }

            @Override
            public void withPipe(final MessagePipe<K, T, ?> pipe) {

            }

            @Override
            public void withPipe(final SingleMessagePipe<T, ?> pipe) {

            }
        }

        private class MessageProcessingBuilderImpl<K extends Connection<T>, T, R> implements MessageProcessingBuilder<K, T, R> {

            private final MessagePipe<K, T, R> pipe;
            private MessageProcessingBuilderImpl<K, T, ?> child;

            private MessageProcessingBuilderImpl(final MessagePipe<K, T, R> pipe) {
                this.pipe = pipe;
            }

            @Override
            public <NEW_R> MessageProcessingBuilder<K, T, NEW_R> withPipe(final MessagePipe<K, ? super R, ? extends NEW_R> f) {
                return null;
            }

            @Override
            public MessageProcessingBuilder<K, T, R> consume(final Consumer<R> consumer) {
                final var builder = new MessageProcessingBuilderImpl(pipe.consume(consumer));
                child = builder;
                return builder;
            }

            @Override
            public MessageProcessingBuilder<K, T, R> consume(final BiConsumer<K, R> consumer) {
                final var builder = new MessageProcessingBuilderImpl(pipe.consume(consumer));
                child = builder;
                return builder;
            }

            @Override
            public <NEW_R> MessageProcessingBuilder<K, T, NEW_R> map(final Function<? super R, ? extends NEW_R> f){
                final var builder = new MessageProcessingBuilderImpl(pipe.map(f));
                child = builder;
                return builder;
            }

            @Override
            public <NEW_R> MessageProcessingBuilder<K, T, NEW_R> map(final BiFunction<K, ? super R, ? extends NEW_R> f) {
                final var builder = new MessageProcessingBuilderImpl(pipe.map(f));
                child = builder;
                return builder;
            }

            private MessagePipe<K, T, ?> getFinalPipe() {
                if (child == null) {
                    return pipe;
                }

                return child.getFinalPipe();
            }
        }
    }

}
