package dev.mardroemmar.lexica;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.MinLen;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A bi-directional immutable map of unique keys to unique values.
 *
 * @param <K> the type of key
 * @param <V> the type of value
 * @since 0.1.0
 */
@API(status = Status.MAINTAINED, since = "0.1.0")
public final class Index<K, V> {
  private static final Index<?, ?> EMPTY = new Index<>();

  private final Map<K, V> keyToValue;
  // The keyToValue map should contain the same info, just other way around: ignore this field for equals/hashCode.
  private final Map<V, K> valueToKey;

  private @Nullable Index<V, K> reversed;

  private Index() {
    this.keyToValue = Map.of();
    this.valueToKey = Map.of();
  }

  private Index(final Map<K, V> keyToValue, final Map<V, K> valueToKey) {
    this.keyToValue = Collections.unmodifiableMap(keyToValue);
    this.valueToKey = Collections.unmodifiableMap(valueToKey);
  }

  /**
   * Gets an empty index.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @return an empty index; this is always referentially the same object
   */
  @SuppressWarnings("unchecked") // We need to cast such here.
  @Pure
  @API(status = Status.INTERNAL, since = "0.1.0")
  private static <K, V> Index<K, V> empty() {
    return (Index<K, V>) EMPTY;
  }

  /**
   * Create an index between type {@code K} and type {@code V} of all variants whose names are the same.
   * <p>
   * This is useful for when two APIs serve their own enums with the same values.
   * <p>
   * <i>Note</i>: Any keys or values that do not have a matching name in the other type will not be mapped.
   *
   * @param keyType   the key type. If {@code null}, behaviour is undefined. If empty, behaviour is undefined.
   * @param valueType the value type. If {@code null}, behaviour is undefined. If empty, behaviour is undefined.
   * @param <K>       the key type
   * @param <V>       the value type
   * @return an index between {@code keyType} and {@code valueType} as described above
   * @since 0.1.0
   */
  public static <K extends Enum<K>, V extends Enum<V>> Index<K, V> enums(
      final Class<K> keyType, final Class<V> valueType
  ) {
    if (keyType.getEnumConstants().length == 0) {
      return empty();
    }

    final var keyToValue = new EnumMap<K, V>(keyType);
    final var valueToKey = new EnumMap<V, K>(valueType);

    // Because we know that a name must occur both in keyType and in valueType to exist in both,
    // we only need to check valueType when iterating over those in keyType: if it's not in the ones we
    // iterate over, it's not there, despite its possible existence in valueType.
    final var values = valueType.getEnumConstants(); // Don't reallocate once per K constant.
    for (final var key : keyType.getEnumConstants()) {
      for (final var value : values) {
        if (key.name().equals(value.name())) {
          keyToValue.put(key, value);
          valueToKey.put(value, key);
        }
      }
    }

    return new Index<>(keyToValue, valueToKey);
  }

  /**
   * Create an index between type {@code K} and type {@code V} with all values provided.
   *
   * @param valueToKeyFunction a function that maps values to their respective keys. This must not return {@code null} at any point;
   *                           behaviour is undefined at that point. If {@code null}, behaviour is undefined.
   * @param values             the available values to map. If {@code null} or any element is {@code null}, behaviour is undefined.
   * @param <K>                the key type
   * @param <V>                the value type
   * @return an index between {@code keyType} and {@code valueType} as described above
   * @since 0.1.0
   */
  public static <K, V> Index<K, V> valueToKey(
      final Function<V, K> valueToKeyFunction, final @MinLen(1) Iterable<V> values
  ) {
    final int size = values instanceof Collection<?> ? ((Collection<?>) values).size() : 16;
    final var keyToValue = new HashMap<K, V>(size);
    final var valueToKey = new HashMap<V, K>(size);

    for (final var value : values) {
      final var key = valueToKeyFunction.apply(value);
      keyToValue.put(key, value);
      valueToKey.put(value, key);
    }

    if (keyToValue.isEmpty()) {
      return empty();
    }

    return new Index<>(keyToValue, valueToKey);
  }

  /**
   * Create an index between type {@code K} and type {@code V} with all values provided.
   *
   * @param valueToKeyFunction a function that maps values to their respective keys. This must not return {@code null} at any point;
   *                           behaviour is undefined at that point. If {@code null}, behaviour is undefined.
   * @param values             the available values to map. If {@code null} or any element is {@code null}, behaviour is undefined.
   * @param <K>                the key type
   * @param <V>                the value type
   * @return an index between {@code keyType} and {@code valueType} as described above
   * @since 0.1.0
   */
  @SafeVarargs
  public static <K, V> Index<K, V> valueToKey(
      final Function<V, K> valueToKeyFunction, final V @MinLen(1) ... values
  ) {
    if (values.length == 0) {
      return empty();
    }

    final var keyToValue = new HashMap<K, V>(values.length);
    final var valueToKey = new HashMap<V, K>(values.length);

    for (final var value : values) {
      final var key = valueToKeyFunction.apply(value);
      keyToValue.put(key, value);
      valueToKey.put(value, key);
    }

    return new Index<>(keyToValue, valueToKey);
  }

  /**
   * Create an index between type {@code K} and type {@code V} with all values provided.
   * <p>
   * <i>Note</i>: This is not intended to be used from Java. Use {@link #valueToKey(Function, Object[])} instead: this is only here for
   * Kotlin users who do not want to use the spread operator, which causes unnecessary copies.
   *
   * @param valueToKeyFunction a function that maps values to their respective keys. This must not return {@code null} at any point;
   *                           behaviour is undefined at that point. If {@code null}, behaviour is undefined.
   * @param values             the available values to map. If {@code null} or any element is {@code null}, behaviour is undefined.
   * @param <K>                the key type
   * @param <V>                the value type
   * @return an index between {@code keyType} and {@code valueType} as described above
   * @since 0.1.0
   */
  // API status is the same as valueToKey, always!
  public static <K, V> Index<K, V> valueToKeyKotlin(
      final Function<V, K> valueToKeyFunction, final V @MinLen(1) [] values
  ) {
    return valueToKey(valueToKeyFunction, values);
  }

  /**
   * Create an index between type {@code K} and type {@code V} with all keys provided.
   *
   * @param keyToValueFunction a function that maps keys to their respective values . This must not return {@code null} at any point;
   *                           behaviour is undefined at that point. If {@code null}, behaviour is undefined.
   * @param keys               the available keys to map. If {@code null} or any element is {@code null}, behaviour is undefined.
   * @param <K>                the key type
   * @param <V>                the value type
   * @return an index between {@code keyType} and {@code valueType} as described above
   * @since 0.1.0
   */
  @SafeVarargs
  public static <K, V> Index<K, V> keyToValue(
      final Function<K, V> keyToValueFunction, final K @MinLen(1) ... keys
  ) {
    if (keys.length == 0) {
      return empty();
    }

    final var keyToValue = new HashMap<K, V>(keys.length);
    final var valueToKey = new HashMap<V, K>(keys.length);

    for (final var key : keys) {
      final var value = keyToValueFunction.apply(key);
      keyToValue.put(key, value);
      valueToKey.put(value, key);
    }

    return new Index<>(keyToValue, valueToKey);
  }

  /**
   * Create an index between type {@code K} and type {@code V} with all keys provided.
   * <p>
   * <i>Note</i>: This is not intended to be used from Java. Use {@link #keyToValue(Function, Object[])} instead: this is only here for
   * Kotlin users who do not want to use the spread operator, which causes unnecessary copies.
   *
   * @param keyToValueFunction a function that maps keys to their respective values . This must not return {@code null} at any point;
   *                           behaviour is undefined at that point. If {@code null}, behaviour is undefined.
   * @param keys               the available keys to map. If {@code null} or any element is {@code null}, behaviour is undefined.
   * @param <K>                the key type
   * @param <V>                the value type
   * @return an index between {@code keyType} and {@code valueType} as described above
   * @since 0.1.0
   */
  // API status is the same as keyToValue, always!
  public static <K, V> Index<K, V> keyToValueKotlin(
      final Function<K, V> keyToValueFunction, final K @MinLen(1) [] keys
  ) {
    return keyToValue(keyToValueFunction, keys);
  }

  /**
   * An immutable view of all keys.
   *
   * @return the keys in this map
   * @since 0.1.0
   */
  @SideEffectFree
  public Set<K> keys() {
    return this.keyToValue.keySet();
  }

  /**
   * An immutable view of all values.
   *
   * @return the values in this map
   * @since 0.1.0
   */
  @SideEffectFree
  public Set<V> values() {
    return this.valueToKey.keySet();
  }

  /**
   * Gets the key for a given value.
   * <p>
   * May be {@code null} if the value is not mapped.
   *
   * @param value the value to get the key for. If {@code null}, behaviour is undefined.
   * @return the key of {@code value}, or {@code null} if unknown
   * @since 0.1.0
   */
  @Pure
  public @Nullable K key(final V value) {
    return this.valueToKey.get(value);
  }

  /**
   * Gets the value for a given key.
   * <p>
   * May be {@code null} if {@code key} is not mapped.
   *
   * @param key the key to get the value for. If {@code null}, behaviour is undefined.
   * @return the value of {@code key}, or {@code null} if unknown
   * @since 0.1.0
   */
  @Pure
  public @Nullable V value(final K key) {
    return this.keyToValue.get(key);
  }

  /**
   * This index, but reversed: keys become values, and values become keys.
   * <p>
   * <i>Note</i>: This keeps the original index in memory.
   *
   * @return an index which is the same as this but reversed
   * @since 0.1.0
   */
  @Pure
  public Index<V, K> reversed() {
    if (this.reversed == null) {
      this.reversed = new Index<>(this.valueToKey, this.keyToValue);
      this.reversed.reversed = this;
    }
    return this.reversed;
  }

  @Override
  public String toString() {
    return "Index{"
        + this.keyToValue.entrySet()
        .stream()
        .map(e -> e.getKey() + ": " + e.getValue())
        .collect(Collectors.joining(", "))
        + '}';
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    final Index<?, ?> index = (Index<?, ?>) o;
    return this.keyToValue.equals(index.keyToValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.keyToValue);
  }
}
