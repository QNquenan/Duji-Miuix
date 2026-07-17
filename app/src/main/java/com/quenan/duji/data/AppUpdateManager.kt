package com.quenan.duji.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

object AppUpdateManager {
    private const val DOWNLOAD_URL =
        "https://gh-proxy.org/https://github.com/QNquenan/Duji-Miuix/releases/latest/download/app-release.apk"
    private const val APK_FILE_NAME = "app-release.apk"
    private const val PREFERENCES_NAME = "app_update"
    private const val DOWNLOAD_ID_KEY = "download_id"
    private const val APK_PATH_KEY = "apk_path"
    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"

    fun enqueue(context: Context, versionName: String): Long {
        val versionTag = ReleaseNotesRepository.versionTag(versionName)
        val downloadUrl = DOWNLOAD_URL
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

        val downloadId = context.getSystemService(DownloadManager::class.java).enqueue(request)
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

        installPending(context)
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
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }

        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(apkUri, APK_MIME_TYPE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        )
        preferences.edit().clear().apply()
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
