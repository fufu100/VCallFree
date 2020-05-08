package ufree.call.international.phone.wifi.vcallfree.utils

import com.newmotor.x5.db.DBHelper
import com.translate.english.voice.lib.prefs
import ufree.call.international.phone.wifi.vcallfree.api.Country
import ufree.call.international.phone.wifi.vcallfree.api.User

/**
 * Created by lyf on 2020/5/7.
 */
class UserManager {
    companion object {
        private var instance: UserManager? = null
            get() {
                if (field == null) {
                    field = UserManager()
                    if(field != null) {
                        val iso = prefs.getStringValue("iso", "")
                        if (iso.isNotEmpty()) {
                            field?.country = DBHelper.get().getCountry(iso)
                        }
                    }
                }

                return field
            }
        @JvmStatic
        fun get(): UserManager{
            return instance!!
        }
    }
    var user:User? = null
    var country:Country? = null
}