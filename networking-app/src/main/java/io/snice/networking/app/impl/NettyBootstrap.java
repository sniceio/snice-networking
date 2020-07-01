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

public class NettyBootstrap<T, C extends NetworkAppConfig> implements NetworkBootstrap<T, C> {

    private SerializationFactory<T> serializationFactory;

    private final C config;

    private final List<ConnectionCtxBuilder<Connection<T>, T, ?>> rules = new ArrayList<>();

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
    public ConnectionContext.Builder<Connection<T>, T, T> onConnection(final Predicate<ConnectionId> condition) {
        assertNotNull(condition, "The condition cannot be null");
        final ConnectionCtxBuilder<Connection<T>, T, T> builder = new ConnectionCtxBuilder<>(condition);
        rules.add(builder);
        return builder;
    }

    private static class ConnectionCtxBuilder<C extends Connection<T>, T, R> implements ConnectionContext.Builder<C, T, R> {

        private final Predicate<ConnectionId> condition;
        private Function<C, T> dropFunction;
        private Consumer<ConnectionContext.ConfigurationBuilder<C, T, R>> confBuilderConsumer;

        private ConnectionCtxBuilder(final Predicate<ConnectionId> condition) {
            this.condition = condition;
        }

        public ConnectionContext<C, T> build() {
            System.out.println("Building the connetion context");
            final List<MessagePipe<C, T, ?>> rules;
            if (confBuilderConsumer != null) {
                final ConfBuilder b = new ConfBuilder();
                confBuilderConsumer.accept(b);
                rules = b.getRules();
                System.out.println("The rules are: " + rules);
            } else {
                rules = List.of();
            }

            return new NettyConnectionContext<C, T>(condition, dropFunction, rules);
        }

        @Override
        public void accept(final Consumer<ConnectionContext.ConfigurationBuilder<C, T, R>> consumer) {
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
        public void drop(final Function<C, T> f) {
            assertArgument(confBuilderConsumer == null, "You have already marked this connection to be accepted, " +
                    "you cannot also drop the connection. You have to choose one or the other");
            assertArgument(dropFunction == null, "You cannot specify to drop the connection twice");
            assertNotNull(f, "The drop function cannot be null");
            dropFunction = f;
        }

        private class ConfBuilder implements ConnectionContext.ConfigurationBuilder<C, T, R> {

            private final List<MessageProcessingBuilderImpl<C, T, T>> rules = new ArrayList<>();

            @Override
            public ConnectionContext.ConfigurationBuilder<C, T, R> withDefaultStatisticsModule() {
                return this;
            }

            private List<MessagePipe<C, T, ?>> getRules() {
                return rules.stream().map(MessageProcessingBuilderImpl::getFinalPipe).collect(Collectors.toList());
            }

            @Override
            public MessageProcessingBuilder<C, T, R> match(final Predicate<T> filter) {
                final BiPredicate<C, T> filter2 = (c, t) -> filter.test(t);
                final MessagePipe<C, T, T> initialPipe = MessagePipe.match(filter2);
                final MessageProcessingBuilderImpl builder = new MessageProcessingBuilderImpl(initialPipe);
                rules.add(builder);
                return builder;
            }

            @Override
            public void withPipe(final MessagePipe<C, T, ?> pipe) {

            }

            @Override
            public void withPipe(final SingleMessagePipe<T, ?> pipe) {

            }
        }

        private class MessageProcessingBuilderImpl<C extends Connection, T, R> implements MessageProcessingBuilder<C, T, R> {

            private final MessagePipe<C, T, R> pipe;
            private MessageProcessingBuilderImpl<C, T, ?> child;

            private MessageProcessingBuilderImpl(final MessagePipe<C, T, R> pipe) {
                this.pipe = pipe;
            }

            @Override
            public <NEW_R> MessageProcessingBuilder<C, T, NEW_R> withPipe(final MessagePipe<C, ? super R, ? extends NEW_R> f) {
                return null;
            }

            @Override
            public MessageProcessingBuilder<C, T, R> consume(final Consumer<R> consumer) {
                final var builder = new MessageProcessingBuilderImpl(pipe.consume(consumer));
                child = builder;
                return builder;
            }

            @Override
            public MessageProcessingBuilder<C, T, R> consume(final BiConsumer<C, R> consumer) {
                final var builder = new MessageProcessingBuilderImpl(pipe.consume(consumer));
                child = builder;
                return builder;
            }

            @Override
            public <NEW_R> MessageProcessingBuilder<C, T, NEW_R> map(final Function<? super R, ? extends NEW_R> f){
                final var builder = new MessageProcessingBuilderImpl(pipe.map(f));
                child = builder;
                return builder;
            }

            @Override
            public <NEW_R> MessageProcessingBuilder<C, T, NEW_R> map(final BiFunction<C, ? super R, ? extends NEW_R> f) {
                final var builder = new MessageProcessingBuilderImpl(pipe.map(f));
                child = builder;
                return builder;
            }

            private MessagePipe<C, T, ?> getFinalPipe() {
                if (child == null) {
                    return pipe;
                }

                return child.getFinalPipe();
            }
        }
    }

}
