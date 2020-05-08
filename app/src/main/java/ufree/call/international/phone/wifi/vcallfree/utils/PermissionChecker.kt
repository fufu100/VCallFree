package ufree.call.international.phone.wifi.vcallfree.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionChecker(val context: Context) {

    fun lackPermissions(permissions:Array<String>):Boolean{
        for(p in permissions){
            if(lackPermission(p)){
                return true
            }
        }
        return false
    }

    fun lackPermission(persmission:String):Boolean{
        return ContextCompat.checkSelfPermission(context,persmission) == PackageManager.PERMISSION_DENIED
    }
}