package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.MessagePipe;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class NettyConnectionContext<C extends Connection<T>, T> implements ConnectionContext<C, T> {

    private final Predicate<ConnectionId> condition;
    private final Function<C, T> dropFunction;

    final List<MessagePipe<C, T, ?>> rules;

    public NettyConnectionContext(final Predicate<ConnectionId> condition, final Function<C, T> dropFunction, final List<MessagePipe<C, T, ?>> rules) {
        this.condition = condition;
        this.dropFunction = dropFunction;
        this.rules = rules;
    }

    @Override
    public MessagePipe<C, T, ?> match(final C connection, final T data) {
        // TODO: insert default rule...
        return rules.stream().filter(pipe -> pipe.test(connection, data)).findFirst().orElseThrow(RuntimeException::new);
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
