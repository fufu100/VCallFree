package vcall.free.international.phone.wifi.calling.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import vcall.free.international.phone.wifi.calling.lib.App
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

class CrashHandler : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: Context? = null

    companion object{
        private lateinit var INSTANCE: CrashHandler
        fun getInstance(): CrashHandler {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = CrashHandler()
            }
            return INSTANCE
        }
    }

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context) {
        mContext = context
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()// 获取系统默认的UncaughtException处理器
        Thread.setDefaultUncaughtExceptionHandler(this)// 设置该CrashHandler为程序的默认处理器
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        ex.printStackTrace()
        if (!handleException(ex) && mDefaultHandler != null) {
            save2File(getCrashInfo(mContext!!, ex))
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            try {
                Thread.sleep(3000)// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }

    fun handleException(ex: Throwable?): Boolean {
        if (ex == null || mContext == null)
            return false
        object : Thread() {
            override fun run() {
                //				Looper.prepare();
                //				if(LogUtils.LOG_ON)
                //					ToastUtils.showToast(mContext, "抱歉，程序出现异常即将退出！", 0);
                try {
                    val crashInfo = getCrashInfo(mContext!!, ex)
                    //				DebugProvider.saveExcInfo("UnCounghtException", crashInfo);
                    save2File(crashInfo)
                    //				sendAppCrashReport(mContext,crashInfo,file);
                    //				Looper.loop();
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }.start()
        return true
    }

    //保存异常信息到sd卡
    private fun save2File(crashInfo: String?): File? {
        val simpleFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())
        val time = simpleFormatter.format(Date())
        val fileName = "crash-$time.log"
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            try {
//                val path = FileUtils.
                val file = File(App.appCacheDirectory, fileName)
                FileUtils.makesureFileExist(file)
                val fos = FileOutputStream(file)
                fos.write(crashInfo!!.toByteArray())
                fos.close()
                return file
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return null
    }

    //获取异常信息
    private fun getCrashInfo(context: Context, ex: Throwable?): String? {
        try {
            val sb = StringBuffer()
            val pm = context.packageManager
            val pi = pm.getPackageInfo( context.packageName,0)
            if (pi != null) {
                sb.append("Version:" + pi.versionName + "\n")
                sb.append("VersionCode:" + pi.versionCode + "\n")
                sb.append(
                    "android:" + android.os.Build.VERSION.RELEASE + "("
                            + android.os.Build.MODEL + ")\n"
                )
                sb.append("Exception:" + ex!!.message + "\n")
            }
            val info = StringWriter()
            val printWriter = PrintWriter(info)
            ex!!.printStackTrace(printWriter)

            var cause: Throwable? = ex.cause
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            sb.append(info.toString())
            printWriter.close()
            return sb.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }
}