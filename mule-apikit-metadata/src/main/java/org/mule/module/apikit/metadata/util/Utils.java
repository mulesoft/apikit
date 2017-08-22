package org.mule.module.apikit.metadata.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private Utils() {}

    public static <K, V> Map<K, V> merge(List<Map<K, V>> maps) {
        final Map<K, V> map = new HashMap<>();

        maps.forEach(map::putAll);

        return map;
    }

}
