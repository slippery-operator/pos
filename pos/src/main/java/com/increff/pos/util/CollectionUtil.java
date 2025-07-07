package com.increff.pos.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for common collection operations.
 * Provides generic methods for grouping, filtering, and manipulating collections.
 */
public class CollectionUtil {

    /**
     * Groups a list of items by a key extracted using the provided function.
     * 
     * @param items The list of items to group
     * @param keyExtractor Function to extract the grouping key from each item
     * @param <T> The type of items in the list
     * @param <K> The type of the grouping key
     * @return Map where keys are the extracted values and values are lists of items with that key
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> items, Function<T, K> keyExtractor) {
        Map<K, List<T>> grouped = new HashMap<>();
        for (T item : items) {
            K key = keyExtractor.apply(item);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return grouped;
    }

    /**
     * Counts occurrences of each key extracted from a list of items.
     * 
     * @param items The list of items to count
     * @param keyExtractor Function to extract the key from each item
     * @param <T> The type of items in the list
     * @param <K> The type of the key
     * @return Map where keys are the extracted values and values are their counts
     */
    public static <T, K> Map<K, Integer> countBy(List<T> items, Function<T, K> keyExtractor) {
        Map<K, Integer> counts = new HashMap<>();
        for (T item : items) {
            K key = keyExtractor.apply(item);
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        return counts;
    }

    /**
     * Filters a list based on a predicate and returns only the items that pass the filter.
     * 
     * @param items The list of items to filter
     * @param predicate Function that returns true for items to keep
     * @param <T> The type of items in the list
     * @return List containing only the items that pass the predicate
     */
    public static <T> List<T> filter(List<T> items, Function<T, Boolean> predicate) {
        List<T> filtered = new ArrayList<>();
        for (T item : items) {
            if (predicate.apply(item)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
} 