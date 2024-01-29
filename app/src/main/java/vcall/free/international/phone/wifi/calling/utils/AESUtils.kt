package vcall.free.international.phone.wifi.calling.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"

    private fun generateKeyFromString(input: String): String {
        val trimmed = input.take(16)
        return trimmed.padEnd(16, '0')
    }

    @Throws(Exception::class)
    fun encrypt(key: String, value: String): String {
        val newKey = generateKeyFromString(key)
        val secretKeySpec = SecretKeySpec(newKey.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)

        val encryptedValue = cipher.doFinal(value.toByteArray())

        return Base64.encodeToString(encryptedValue,Base64.NO_WRAP)
    }

    @Throws(Exception::class)
    fun decrypt(key: String, encryptedValue: String): String {
        val newKey = generateKeyFromString(key)
        val secretKeySpec = SecretKeySpec(newKey.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)

        val originalValue = cipher.doFinal(Base64.decode(encryptedValue,Base64.NO_WRAP))

        return String(originalValue)
    }
}
