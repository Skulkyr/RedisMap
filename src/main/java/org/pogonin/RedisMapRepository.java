package org.pogonin;


import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.util.*;

import org.jetbrains.annotations.NotNull;

/**
 * A Redis-backed implementation of a {@link Map} with {@link String} keys and values.
 * This class provides a way to use Redis as a map-like data structure, with keys
 * and values stored within a specified Redis namespace.
 *
 * <p>
 * Note: This implementation does not support null keys or values.
 * </p>
 *
 * <p>
 * This class implements the {@link Closeable} interface, allowing for
 * the underlying Jedis connection to be properly closed when
 * it is no longer needed.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * RedisMapRepository map = new RedisMapRepository("localhost", 6379, "myNamespace");
 *
 * map.put("key1", "value1");
 * String value = map.get("key1");
 *
 * map.close();
 * }</pre>
 *
 * @see Map
 * @see Closeable
 */
public class RedisMapRepository implements Map<String, String>, Closeable {
    private final Jedis jedis;
    private final String redisNamespace;

    public RedisMapRepository(String host, int port, String redisNamespace) {
        this.jedis = new Jedis(host, port);
        this.redisNamespace = redisNamespace;
    }

    @Override
    public int size() {
        return jedis.keys(redisNamespace + ":*").size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        String fullKey = getFullKey((String) key);
        return jedis.exists(fullKey);
    }

    @Override
    public boolean containsValue(Object value) {
        for (String key : jedis.keys(redisNamespace + ":*")) {
            if (jedis.get(key).equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String get(Object key) {
        String fullKey = getFullKey((String) key);
        return jedis.get(fullKey);
    }

    @Override
    public String put(String key, String value) {
        String fullKey = getFullKey(key);
        return jedis.set(fullKey, value);
    }

    @Override
    public String remove(Object key) {
        String fullKey = getFullKey((String) key);
        jedis.del(fullKey);
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for (String key : jedis.keys(redisNamespace + ":*")) {
            jedis.del(key);
        }
    }

    @Override
    @NotNull
    public Set<String> keySet() {
        Set<String> fullKeys = jedis.keys(redisNamespace + ":*");
        Set<String> keys = new HashSet<>();
        for (String fullKey : fullKeys) {
            keys.add(fullKey.substring(redisNamespace.length() + 1));
        }
        return keys;
    }

    @Override
    @NotNull
    public Collection<String> values() {
        Set<String> keys = keySet();
        Set<String> values = new HashSet<>();
        for (String key : keys) {
            values.add(get(key));
        }
        return values;
    }


    @Override
    @NotNull
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> entrySet = new HashSet<>();
        for (String key : keySet()) {
            entrySet.add(new AbstractMap.SimpleEntry<>(key, get(key)));
        }
        return entrySet;
    }

    private String getFullKey(String key) {
        return redisNamespace + ":" + key;
    }

    public void close() {
        jedis.close();
    }
}
