package com.quenan.duji.data

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.quenan.duji.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference

object AppUpdateManager {
    private const val DOWNLOAD_URL_PREFIX =
        "https://gh-proxy.org/https://github.com/QNquenan/Duji-Miuix/releases/download/"
    private const val LATEST_DOWNLOAD_URL =
        "https://gh-proxy.org/https://github.com/QNquenan/Duji-Miuix/releases/latest/download/app-release.apk"
    private const val APK_FILE_NAME = "app-release.apk"
    private const val PREFERENCES_NAME = "app_update"
    private const val DOWNLOAD_ID_KEY = "download_id"
    private const val APK_PATH_KEY = "apk_path"
    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    private const val UPDATE_CHANNEL_ID = "app_updates"
    private const val UPDATE_CHANNEL_NAME = "应用更新"
    private const val UPDATE_NOTIFICATION_ID = 1001

    @Volatile
    private var installHostReference: WeakReference<Activity>? = null

    fun createNotificationChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                UPDATE_CHANNEL_ID,
                UPDATE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "应用更新下载完成和安装提醒"
            },
        )
    }

    fun registerInstallHost(activity: Activity) {
        installHostReference = WeakReference(activity)
    }

    fun unregisterInstallHost(activity: Activity) {
        if (installHostReference?.get() === activity) {
            installHostReference = null
        }
    }

    suspend fun enqueue(context: Context, versionName: String): Long {
        val versionTag = ReleaseNotesRepository.versionTag(versionName)
        val releaseTag = runCatching {
            ReleaseNotesRepository.fetchLatestReleaseTag()
        }.getOrNull()?.let { ReleaseNotesRepository.versionTag(it) }
        val downloadUrl = releaseTag?.let {
            "$DOWNLOAD_URL_PREFIX$it/$APK_FILE_NAME"
        } ?: LATEST_DOWNLOAD_URL
        val downloadDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: error("无法访问应用下载目录")
        val apkFile = File(downloadDirectory, "DuJi-$versionTag-$APK_FILE_NAME")
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("DuJi ${versionTag ?: versionName}")
            .setDescription("正在下载更新")
            .setMimeType(APK_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                apkFile.name,
            )

        val downloadId = withContext(Dispatchers.Main) {
            context.getSystemService(DownloadManager::class.java).enqueue(request)
        }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(DOWNLOAD_ID_KEY, downloadId)
            .putString(APK_PATH_KEY, apkFile.absolutePath)
            .apply()
        return downloadId
    }

    fun handleDownloadComplete(context: Context, downloadId: Long) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (preferences.getLong(DOWNLOAD_ID_KEY, -1L) != downloadId) return

        val installHost = installHostReference?.get()
        if (installHost != null && !installHost.isFinishing && !installHost.isDestroyed) {
            installPending(installHost)
        } else {
            showInstallNotification(context)
        }
    }

    fun installPending(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val downloadId = preferences.getLong(DOWNLOAD_ID_KEY, -1L)
        if (downloadId < 0L) return
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query)?.use { cursor ->
            if (!cursor.moveToFirst()) return
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            if (status != DownloadManager.STATUS_SUCCESSFUL) return
        } ?: return

        val apkPath = preferences.getString(APK_PATH_KEY, null) ?: return
        val apkFile = File(apkPath)
        if (!apkFile.isFile) return

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            launchUpdateActivity(
                context,
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ),
            )
            return
        }

        launchUpdateActivity(
            context,
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(apkUri, APK_MIME_TYPE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
        )
        preferences.edit().clear().apply()
    }

    private fun launchUpdateActivity(context: Context, intent: Intent) {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun showInstallNotification(context: Context) {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        createNotificationChannel(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            UPDATE_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = android.app.Notification.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("更新下载完成")
            .setContentText("点击安装新版本")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(UPDATE_NOTIFICATION_ID, notification)
    }
}

class AppUpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            AppUpdateManager.handleDownloadComplete(
                context.applicationContext,
                intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L),
            )
        }
    }
}
