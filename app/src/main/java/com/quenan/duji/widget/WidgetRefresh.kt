package com.quenan.duji.widget

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun Context.refreshWidgetsAsync() {
    val appContext = applicationContext
    CoroutineScope(Dispatchers.Default).launch {
        WidgetUpdateDispatcher.refreshAll(appContext)
    }
}
