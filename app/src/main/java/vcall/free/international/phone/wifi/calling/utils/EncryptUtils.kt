package vcall.free.international.phone.wifi.calling.utils

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object EncryptUtils {
    //- 获取签名
    fun generateSignature(uuid: String, packageName: String, timestamp: String, secretKey: String): String {
        val message = "$uuid$packageName$timestamp"

        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)

        val bytes = mac.doFinal(message.toByteArray())
//        return Base64.encodeToString(bytes,Base64.NO_WRAP)
        val md = MessageDigest.getInstance("MD5")

        // 但是这里在进行MD5值的计算过程中，并没有使用完整加密后的string，而是直接使用了bytes这个ByteArray
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun md5(str: String):String{
        val md = MessageDigest.getInstance("MD5")
        return md.digest(str.toByteArray()).joinToString(""){ "%02x".format(it) }
    }
}