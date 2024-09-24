package org.pogonin;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisMapRepositoryTest {
    private RedisMapRepository map;
    @SuppressWarnings("all")
    private final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:latest").withExposedPorts(6379);

    @BeforeAll
    void setup() {
        redisContainer.start();
        map = new RedisMapRepository("localhost", redisContainer.getMappedPort(6379), "test");
    }

    @AfterEach
    void dataClear() {
        map.clear();
    }

    @AfterAll
    void tearDown() {
        map.close();
        redisContainer.stop();
    }

    @Test
    void testPutAndGetElement() {
        String key = "key";
        String value = "value";

        map.put(key, value);
        String result = map.get(key);

        assertEquals(value, result);
    }

    @ParameterizedTest
    @CsvSource({"5", "10", "77"})
    void testGetMapSize(int count) {
        for (int i = 0; i < count; i++) {
            String str = String.valueOf(i);
            map.put(str, str);
        }


        int mapSize = map.size();
        assertEquals(count, mapSize);
    }

    @Test
    void testRemove() {
        map.put("key2", "value2");
        map.remove("key2");
        assertNull(map.get("key2"));
    }

    @Test
    void testContainsKey() {
        map.put("key5", "value5");
        assertTrue(map.containsKey("key5"));
        assertFalse(map.containsKey("key6"));
    }

    @Test
    void testContainsValue() {
        map.put("key7", "value7");
        assertTrue(map.containsValue("value7"));
        assertFalse(map.containsValue("value8"));
    }

    @Test
    void testIsEmpty() {
        assertTrue(map.isEmpty());
        map.put("key9", "value9");
        assertFalse(map.isEmpty());
    }

    @Test
    @SuppressWarnings("all")
    void testClear() {
        map.put("key10", "value10");
        map.clear();
        assertEquals(0, map.size());
    }

    @Test
    void testPutAll() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("key11", "value11");
        testMap.put("key12", "value12");
        map.putAll(testMap);
        assertEquals(2, map.size());
        assertEquals("value11", map.get("key11"));
        assertEquals("value12", map.get("key12"));
    }

    @Test
    void testKeySet() {
        map.put("key13", "value13");
        map.put("key14", "value14");
        assertTrue(map.containsKey("key13"));
        assertTrue(map.containsKey("key14"));
    }

    @Test
    void testEntrySet() {
        map.put("key17", "value17");
        map.put("key18", "value18");
        assertTrue(map.entrySet().contains(Map.entry("key17", "value17")));
        assertTrue(map.entrySet().contains(Map.entry("key18", "value18")));
    }
}
