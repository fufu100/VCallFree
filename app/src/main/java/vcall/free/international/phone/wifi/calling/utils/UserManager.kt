package vcall.free.international.phone.wifi.calling.utils

import vcall.free.international.phone.wifi.calling.db.DBHelper
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.api.Country
import vcall.free.international.phone.wifi.calling.api.User

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