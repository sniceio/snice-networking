package io.snice.networking.app;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;


/**
 * @author jonas@jonasborjesson.com
 */
public interface MessagePipe<C, T, R> extends BiFunction<C, T, R>, BiPredicate<C, T> {

	static <C, T, R> MessagePipe<C, T, R> transform(final Function<T, R> f) {
		ensureNotNull(f, "The function cannot be null");
		return new DefaultMessagePipe((c, t) -> true).map(f);
	}

	/**
	 * Create a new {@link MessagePipe} that consumes values of the given type.
	 *
	 * @param inType the type this {@link MessagePipe} will consume.
	 * @param <T>    the type
	 * @return the next step in this process of building up a valid {@link MessagePipe}
	 */
	static <T> OutTypeStep<T> consumes(final Class<T> inType) {
		ensureNotNull(inType, "The input type cannot be null");
		return null;
	}

	static <T1, T2> BiOutTypeStep<T1, T2> consumes(final Class<T1> inType1, final Class<T2> inType2) {
		ensureNotNull(inType1, "The input type cannot be null");
		ensureNotNull(inType2, "The input type cannot be null");
		return null;
	}

	interface OutTypeStep<T> {
		<R> MappingStep<T, R> produces(final Class<R> outType);
	}

	interface BiOutTypeStep<T1, T2> {
		<R> MappingStep<T1, R> produces(final Class<R> outType);
	}

	static <C, T> MessagePipe<C, T, T> match(final BiPredicate<C, T> condition) {
		ensureNotNull(condition, "The condition cannot be null");
		return new DefaultMessagePipe(condition);
	}

	static <C, T, R> MessagePipe<C, T, R> of(final Class<T> inType, final Class<R> outType, final BiPredicate<C, T> condition) {
		ensureNotNull(condition, "The condition cannot be null");
		return new DefaultMessagePipe(condition);
	}

	interface MappingStep<T, R> {
		MessagePipe<T, T, R> map(Function<? super T, ? extends R> f);
	}

	interface BiMappingStep<C, T, R> {
		MessagePipe<C, T, R> map(Function<? super T, ? extends R> f);
	}

	MessagePipe<C, T, R> consume(Consumer<R> f);

	MessagePipe<C, T, R> consume(BiConsumer<C, R> f);

	<NEW_R> MessagePipe<C, T, NEW_R> map(Function<? super R, ? extends NEW_R> f);

	<NEW_R> MessagePipe<C, T, NEW_R> map(BiFunction<C, ? super R, ? extends NEW_R> f);


	class DefaultMessagePipe<C, T, V, R> implements MessagePipe<C, T, R> {

		private final BiFunction<C, T, V> parentFunction;

		private final BiPredicate<C, T> condition;

		private final BiFunction<C, V, R> function;

		private final BiConsumer<C, R> consumer;

		private static final BiFunction NULL_FUNCTION = (c, r) -> r;

		private static final BiConsumer NULL_CONSUMER = (c, r) -> {
		};

		private DefaultMessagePipe(final BiPredicate<C, T> condition) {
			this(NULL_FUNCTION, condition, NULL_FUNCTION, NULL_CONSUMER);
		}

		private DefaultMessagePipe(final BiFunction<C, T, V> parentFunction,
				final BiPredicate<C, T> condition,
				final BiFunction<C, V, R> function,
				final BiConsumer<C, R> consumer) {
			this.parentFunction = parentFunction;
			this.condition = condition;
			this.function = function;
			this.consumer = consumer;
		}

		@Override
		public MessagePipe<C, T, R> consume(final Consumer<R> f) {
			assertNotNull(f, "The function cannot be null");
			final BiConsumer<C, R> biConsumer = (c, r) -> f.accept(r);
			return new DefaultMessagePipe<>(this, condition, NULL_FUNCTION, biConsumer);
		}

		@Override
		public MessagePipe<C, T, R> consume(final BiConsumer<C, R> f) {
			assertNotNull(f, "The function cannot be null");
			return new DefaultMessagePipe<>(this, condition, NULL_FUNCTION, f);
		}

		@Override
		public <NEW_R> MessagePipe<C, T, NEW_R> map(final Function<? super R, ? extends NEW_R> f) {
			ensureNotNull(f, "The function cannot be null");
			final BiFunction<C, ? super R, ? extends NEW_R> biF = (c, r) -> f.apply(r);
			return new DefaultMessagePipe(this, condition, biF, NULL_CONSUMER);
		}

		@Override
		public <NEW_R> MessagePipe<C, T, NEW_R> map(final BiFunction<C, ? super R, ? extends NEW_R> f) {
			ensureNotNull(f, "The function cannot be null");
			return new DefaultMessagePipe(this, condition, f, NULL_CONSUMER);
		}

		@Override
		public R apply(final C c, final T t) {
			final V parentResult = parentFunction.apply(c, t);
			final R result = function.apply(c, parentResult);
			consumer.accept(c, result);
			return result;
		}

		@Override
		public boolean test(final C c, final T t) {
			return condition.test(c, t);
		}

	}
}
