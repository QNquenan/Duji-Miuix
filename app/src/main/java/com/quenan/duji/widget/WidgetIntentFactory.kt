package com.quenan.duji.widget

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.quenan.duji.widget.ui.WidgetEntryActivity

object WidgetIntentFactory {
    fun detailAction(
        context: Context,
        targetType: WidgetTargetType,
        targetId: Long,
    ): Action {
        val intent = Intent(context, WidgetEntryActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "duji://widget/${targetType.name.lowercase()}/$targetId".toUri()
            putExtra(WidgetEntryActivity.EXTRA_TARGET_TYPE, targetType.name)
            putExtra(WidgetEntryActivity.EXTRA_TARGET_ID, targetId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return actionStartActivity(intent)
    }
}

enum class WidgetTargetType {
    ITEM,
    DAY,
}
