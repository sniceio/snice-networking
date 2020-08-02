package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.MessagePipe;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class NettyConnectionContext<K extends Connection<T>, T> implements ConnectionContext<K, T> {

    private final Predicate<ConnectionId> condition;
    private final Function<K, T> dropFunction;

    final List<MessagePipe<K, T, ?>> rules;

    final List<MessagePipe<K, Object, ?>> eventRules;

    public NettyConnectionContext(final Predicate<ConnectionId> condition, final Function<K, T> dropFunction,
                                  final List<MessagePipe<K, T, ?>> rules,
                                  final List<MessagePipe<K, Object, ?>> eventRules) {
        this.condition = condition;
        this.dropFunction = dropFunction;
        this.rules = rules;
        this.eventRules = eventRules;
    }

    @Override
    public MessagePipe<K, T, ?> match(final K connection, final T data) {
        // TODO: insert default rule...
        return rules.stream().filter(pipe -> pipe.test(connection, data)).findFirst().orElseThrow(RuntimeException::new);
    }

    @Override
    public <U extends Object> MessagePipe<K, U, ?> matchEvent(final K connection, final U event) {
        // TODO: insert default rule...
        return (MessagePipe<K, U, ?>) eventRules.stream().filter(pipe -> pipe.test(connection, event)).findFirst().orElseThrow(RuntimeException::new);
    }

    @Override
    public boolean isDrop() {
        return dropFunction != null;
    }

    @Override
    public boolean test(final ConnectionId connectionId) {
        return condition.test(connectionId);
    }
}
