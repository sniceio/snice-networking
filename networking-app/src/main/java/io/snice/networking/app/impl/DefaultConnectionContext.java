package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.MessagePipe;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultConnectionContext<K extends Connection<T>, T> implements ConnectionContext<K, T> {

    private final Predicate<ConnectionId> condition;

    private final Optional<Function<K, T>> dropFunction;

    final List<MessagePipe<K, T, ?>> rules;

    final List<MessagePipe<K, Object, ?>> eventRules;

    public DefaultConnectionContext(final Predicate<ConnectionId> condition, final Function<K, T> dropFunction,
                                    final List<MessagePipe<K, T, ?>> rules,
                                    final List<MessagePipe<K, Object, ?>> eventRules) {
        this.condition = condition;
        this.dropFunction = Optional.ofNullable(dropFunction);
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
    public Predicate<ConnectionId> getPredicate() {
        return condition;
    }

    @Override
    public Optional<Function<K, T>> getDropFunction() {
        return dropFunction;
    }

    @Override
    public boolean test(final ConnectionId connectionId) {
        return condition.test(connectionId);
    }
}
