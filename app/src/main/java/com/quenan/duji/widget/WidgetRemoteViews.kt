package com.quenan.duji.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

internal object WidgetRemoteViews {
    fun setClick(
        context: Context,
        views: RemoteViews,
        viewId: Int,
        intent: Intent,
        requestCode: Int,
    ) {
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        views.setOnClickPendingIntent(
            viewId,
            PendingIntent.getActivity(context, requestCode, intent, flags),
        )
    }
}
