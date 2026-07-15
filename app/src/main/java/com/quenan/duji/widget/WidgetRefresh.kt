package com.quenan.duji.widget

import android.content.Context

internal fun Context.refreshWidgetsAsync() {
    WidgetUpdateDispatcher.requestRefresh(applicationContext)
}
