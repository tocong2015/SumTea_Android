package com.sum.stater.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.text.TextUtils

object StaterUtils {
    private var sCurProcessName: String? = null
    fun isMainProcess(context: Context): Boolean {
        val processName = getCurProcessName(context)
        return if (processName != null && processName.contains(":")) {
            false
        } else processName != null && processName == context.packageName
    }


    private fun getCurProcessName(context: Context): String? {
        val procName = sCurProcessName
        if (!TextUtils.isEmpty(procName)) {
            return procName
        }
        try {
            val pid = Process.myPid()
            val mActivityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (appProcess in mActivityManager.runningAppProcesses) {
                if (appProcess.pid == pid) {
                    sCurProcessName = appProcess.processName
                    return sCurProcessName
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sCurProcessName
    }// ignore


}