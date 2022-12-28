package com.joyy.android_project

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.joyy.android_project", appContext.packageName)
        Utils.acquireASensorManagerInstance(appContext);
    }

    //StorageInfo
    fun getStorageDeviceInfo() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext;
        var storageDeviceInfo = StorageDeviceUtils.getStorageDeviceInfo(appContext.filesDir.absolutePath);
    }

    fun getAllStorageDeviceInfo() {

    }

    fun getAllValidStorageDeviceInfo() {

    }

    fun getDataFileSpaceTotalSize() {

    }

    fun getDataFileSpaceFreeSize() {

    }

    fun getUsedAllStorageSize() {

    }

    fun getTotalAllStorageSize() {

    }

    fun getFreeAllStorageSize() {

    }

    fun getStorageUsedPercent() {

    }
}