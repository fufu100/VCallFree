package vcall.free.international.phone.wifi.calling.utils

import android.util.Base64
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
        return Base64.encodeToString(bytes,Base64.NO_WRAP)
    }
}