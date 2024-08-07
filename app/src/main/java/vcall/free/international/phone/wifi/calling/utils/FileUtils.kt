package vcall.free.international.phone.wifi.calling.utils

import android.content.res.AssetManager
import android.util.Log
import vcall.free.international.phone.wifi.calling.lib.App.Companion.appCacheDirectory
import java.io.*
import java.nio.charset.Charset

object FileUtils {
    fun saveDataAsFile(d: Any, fileName: String) {
        val file = File(appCacheDirectory + File.separator + fileName)
        try {
            makesureFileExist(file)
            val oos = ObjectOutputStream(
                FileOutputStream(file)
            )
            oos.writeObject(d)
            oos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun readDataFromFile(name: String): Any? {
        val file = File(appCacheDirectory + File.separator + name)
        Log.d("readDataFromFile", file.path)
        if (!file.exists()) {
            return null
        }
        try {
            val ois = ObjectInputStream(
                FileInputStream(
                    file
                )
            )
            val data = ois.readObject()
            ois.close()
            return data
        } catch (e: Exception) {
            e.printStackTrace()
            if (file.exists()) {
                file.delete()
                return null
            }
        }

        return null
    }

    fun makesureFileExist(file: File) {
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                val parent = file.parentFile
                if (parent != null && !parent.exists()) {
                    parent.mkdirs()
                }
                try {
                    file.createNewFile()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }

            }

        }

    }

    fun copyFromAssets(
        assets: AssetManager,
        source: String?,
        dest: String,
        isCover: Boolean
    ) {
        val file = File(dest)
        if (isCover || (!isCover && !file.exists())) {
            var `is`: InputStream? = null
            var fos: FileOutputStream? = null
            try {
                `is` = assets.open(source!!)
                fos = FileOutputStream(dest)
                val buffer = ByteArray(1024)
                var size = 0
                while (`is`.read(buffer, 0, 1024).also { size = it } >= 0) {
                    fos.write(buffer, 0, size)
                }
            }catch (e:IOException){
                e.printStackTrace()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } finally {
                        `is`?.close()
                    }
                }
            }
        }
    }

    fun copyFile(res: File?, des: File?) {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = BufferedInputStream(FileInputStream(res))
            os = BufferedOutputStream(FileOutputStream(des))
            val buffer = ByteArray(1024)
            var i: Int
            while (`is`.read(buffer).also { i = it } != -1) {
                os.write(buffer, 0, i)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                `is`!!.close()
                os!!.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun readStringFromInputStream(`in`: InputStream): String? {
        var res: String? = null
        val length: Int
        try {
            length = `in`.available()
            if (length > 0) {
                val buffer = ByteArray(length)
                `in`.read(buffer)
                res = String(buffer, Charset.forName("utf-8"))
            }
            `in`.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return res
    }

    fun readStringFromAssetFile(am: AssetManager, file: String): String? {
        try {
            return readStringFromInputStream(am.open(file))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }

}