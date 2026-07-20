package com.quenan.duji.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE_NAME = "com.quenan.duji"

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startupAndMainScroll() = rule.collect(packageName = PACKAGE_NAME) {
        startActivityAndWait()
        device.waitForIdle()
        repeat(3) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.82f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.22f).toInt(),
                12,
            )
            device.waitForIdle()
        }
    }
}
