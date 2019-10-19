package io.snice.networking.app;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;


/**
 * @author jonas@jonasborjesson.com
 */
public interface SingleMessagePipe<T, R> extends Function<T, R>, Predicate<T> {

    static <T, R> SingleMessagePipe<T, R> transform(final Function<T, R> f) {
        ensureNotNull(f, "The function cannot be null");
        return new DefaultMessagePipe(t -> true).map(f);
    }

    static <T> SingleMessagePipe<T, T> match(final Predicate<T> condition) {
        ensureNotNull(condition, "The condition cannot be null");
        return new DefaultMessagePipe(condition);
    }

    SingleMessagePipe<T, R> consume(Consumer<R> f);

    <NEW_R> SingleMessagePipe<T, NEW_R> map(Function<? super R, ? extends NEW_R> f);

    class DefaultMessagePipe<T, V, R> implements SingleMessagePipe<T, R> {

        private final Function<T, V> parentFunction;

        private final Predicate<T> condition;

        private final Function<V, R> function;

        private final Consumer<R> consumer;

        private static final Function NULL_FUNCTION = r -> r;

        private static final Consumer NULL_CONSUMER = r -> {
        };

        private DefaultMessagePipe(final Predicate<T> condition) {
            this(NULL_FUNCTION, condition, NULL_FUNCTION, NULL_CONSUMER);
        }

        private DefaultMessagePipe(final Function<T, V> parentFunction,
                                   final Predicate<T> condition,
                                   final Function<V, R> function,
                                   final Consumer<R> consumer) {
            this.parentFunction = parentFunction;
            this.condition = condition;
            this.function = function;
            this.consumer = consumer;
        }

        @Override
        public SingleMessagePipe<T, R> consume(final Consumer<R> f) {
            assertNotNull(f, "The function cannot be null");
            final Consumer<R> biConsumer = r -> f.accept(r);
            return new DefaultMessagePipe<>(this, condition, NULL_FUNCTION, biConsumer);
        }

        @Override
        public <NEW_R> SingleMessagePipe<T, NEW_R> map(final Function<? super R, ? extends NEW_R> f) {
            ensureNotNull(f, "The function cannot be null");
            return new DefaultMessagePipe(this, condition, f, NULL_CONSUMER);
        }


        @Override
        public R apply(final T t) {
            final V parentResult = parentFunction.apply(t);
            final R result = function.apply(parentResult);
            consumer.accept(result);
            return result;
        }

        @Override
        public boolean test(final T t) {
            return condition.test(t);
        }

    }
}
