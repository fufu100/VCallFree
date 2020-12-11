package vcall.free.international.phone.wifi.calling.utils

import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import vcall.free.international.phone.wifi.calling.lib.App

/**
 * Created by lyf on 2020/10/19.
 */
class EncryptPrefs {
    val advancedSpec = KeyGenParameterSpec.Builder("your_master_key_name",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).apply {
        setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        setKeySize(256)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val hasStrongBox = App.context!!.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
            if (hasStrongBox)
                setIsStrongBoxBacked(true)
        }
    }.build()
    val advancedKeyAlias = MasterKeys.getOrCreate(advancedSpec)

    init {
        getEncryptedPreferences().edit().putString("key","86101100").apply()
        getEncryptedPreferences().edit().putString("iv","87493871").apply()
    }

    private fun getEncryptedPreferences() =
        EncryptedSharedPreferences.create("your_shared_preferences", advancedKeyAlias,
            App.context!!, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    fun getKey(key:String):String{
        return getEncryptedPreferences().getString(key,"")?:""
    }

}