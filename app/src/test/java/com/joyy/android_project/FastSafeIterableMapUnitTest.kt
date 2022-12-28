package com.joyy.android_project

import com.joyy.android_project.archcore.FastSafeIterableMap
import org.junit.Test

/**
 * author zhaojian@apusapps.com
 * date 2022/12/28
 */
internal class FastSafeIterableMapUnitTest {
    @Test
    fun addition_isCorrect() {
        //FastSafeIterableMap<String, String> map = new FastSafeIterableMap<>;
        val map = FastSafeIterableMap<String, String>();
        map.putIfAbsent("111", "111");
        map.putIfAbsent("222", "222");
        map.putIfAbsent("333", "333");
        map.putIfAbsent("444", "444");
        map.putIfAbsent("555", "555");
        map.putIfAbsent("666", "666");
        map.putIfAbsent("777", "777");
        map.putIfAbsent("888", "888");
        map.putIfAbsent("999", "999");
        map.iterator().forEach {
            println("${it.key} ${it.value}")
            it.setValue("ccc");
        }

        val hashMap = LinkedHashMap<String, String>()
        val iterator = hashMap.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            next.setValue("hhhh")
        }
    }
}