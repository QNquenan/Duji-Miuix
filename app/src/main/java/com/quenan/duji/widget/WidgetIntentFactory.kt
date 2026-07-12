package com.quenan.duji.widget

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.quenan.duji.MainActivity

object WidgetIntentFactory {
    fun detailAction(
        context: Context,
        targetType: WidgetTargetType,
        targetId: Long,
    ): Action {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "duji://widget/${targetType.name.lowercase()}/$targetId".toUri()
            putExtra(EXTRA_START_PAGE, if (targetType == WidgetTargetType.ITEM) 0 else 1)
            putExtra(EXTRA_TARGET_TYPE, targetType.name)
            putExtra(EXTRA_TARGET_ID, targetId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return actionStartActivity(intent)
    }

    fun myItemsPageAction(context: Context): Action {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "duji://widget/my-items".toUri()
            putExtra(EXTRA_START_PAGE, 0)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return actionStartActivity(intent)
    }

    const val EXTRA_START_PAGE = "extra_start_page"
    const val EXTRA_TARGET_TYPE = "extra_target_type"
    const val EXTRA_TARGET_ID = "extra_target_id"
}

enum class WidgetTargetType {
    ITEM,
    DAY,
}
