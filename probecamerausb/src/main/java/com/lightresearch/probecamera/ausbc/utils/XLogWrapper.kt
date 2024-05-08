package com.lightresearch.probecamera.ausbc.utils

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.PatternFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.naming.FileNameGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/**
 * X Log wrapper
 *
 * @author Created by jiangdg on 2022/7/19
 */
object XLogWrapper {
    private const val TAG = "com.lightresearch.probecamera"
    private const val FLATTERER = "{d yyyy-MM-dd HH:mm:ss.SSS} {l}/{t}: {m}"
    private var mHasInit = false
    fun init(application: Application, folderPath: String?) {
        val androidPrinter = AndroidPrinter(true)
        val config = LogConfiguration.Builder().logLevel(LogLevel.ALL)
            .tag(TAG)
            .disableStackTrace().build()
        var path = if (TextUtils.isEmpty(folderPath)) application.getExternalFilesDir(null)!!
            .path else folderPath
        path = if (TextUtils.isEmpty(path)) application.filesDir.path else path
        val filePrinter = FilePrinter.Builder(path)
            .fileNameGenerator(MyFileNameGenerator(application))
            .flattener(MyFlatterer(FLATTERER))
            .build()
        XLog.init(config, androidPrinter, filePrinter)
        mHasInit = true
    }

    fun v(tag: String, msg: String) {
        if (mHasInit) {
            XLog.v("[$tag]  $msg")
            return
        }
        Log.v(tag, "" + msg)
    }

    fun i(tag: String, msg: String) {
        if (mHasInit) {
            XLog.i("[$tag]  $msg")
            return
        }
        Log.i(tag, "" + msg)
    }

    fun d(tag: String, msg: String) {
        if (mHasInit) {
            XLog.d("[$tag]  $msg")
            return
        }
        Log.d(tag, "" + msg)
    }

    fun w(tag: String, msg: String) {
        if (mHasInit) {
            XLog.w("[$tag]  $msg")
            return
        }
        Log.w(tag, "" + msg)
    }

    fun w(tag: String, msg: String, throwable: Throwable?) {
        if (mHasInit) {
            XLog.w("[$tag]  $msg", throwable)
            return
        }
        Log.w(tag, msg, throwable)
    }

    fun w(tag: String, throwable: Throwable?) {
        if (mHasInit) {
            XLog.w("[$tag", throwable)
            return
        }
        Log.w(tag, "", throwable)
    }

    fun e(tag: String, msg: String) {
        if (mHasInit) {
            XLog.e("[$tag]  $msg")
            return
        }
        Log.e(tag, "" + msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable?) {
        if (mHasInit) {
            XLog.e("[$tag]  $msg", throwable)
            return
        }
        Log.e(tag, "" + msg, throwable)
    }

    internal class MyFileNameGenerator(private val mCtx: Context) :
        FileNameGenerator {
        private val mLocalDateFormat: ThreadLocal<SimpleDateFormat?> =
            object : ThreadLocal<SimpleDateFormat?>() {
                override fun initialValue(): SimpleDateFormat {
                    return SimpleDateFormat("yyyy-MM-dd", Locale.US)
                }
            }

        override fun isFileNameChangeable(): Boolean {
            return true
        }

        override fun generateFileName(logLevel: Int, timestamp: Long): String {
            val sdf = mLocalDateFormat.get()
            sdf?.timeZone = TimeZone.getDefault()
            val dateStr = sdf?.format(Date(timestamp))
            return "AUSBC_v" + verName + "_" + dateStr + ".log"
        }

        private val verName: String
            private get() {
                var verName = ""
                try {
                    verName = mCtx.packageManager.getPackageInfo(mCtx.packageName, 0).versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                return verName
            }
    }

    internal class MyFlatterer(pattern: String?) : PatternFlattener(pattern)
}
