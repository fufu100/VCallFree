package vcall.free.international.phone.wifi.calling

import android.annotation.SuppressLint
import com.google.i18n.phonenumbers.PhoneNumberToTimeZonesMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test

import org.junit.Assert.*
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.utils.EncryptUtils
import vcall.free.international.phone.wifi.calling.utils.PointStrategy
import vcall.free.international.phone.wifi.calling.utils.RxUtils
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val uuid = UUID(1L,1L);
        println(uuid.toString())
    }

    @Test
    fun testStrategy() {
        val strategy = PointStrategy()
        val result = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        val probabilities = intArrayOf(10, 100, 150, 350, 350, 30, 9, 1)
        for (i in 0 until 100000) {
            val res = strategy.getRandom(probabilities)
            result[res] = result[res] + 1
        }

        println("testStrategy---")
        result.forEach {
            print(it)
            print(" ")
        }
    }

    @Test
    fun testParsePhone() {
        try {
            val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parseAndKeepRawInput("+12134883500", null)
            val iso = phoneNumberUtil.getRegionCodeForNumber(phoneNumber)
            val phone = phoneNumberUtil.getNationalSignificantNumber(phoneNumber)
            val phoneNationalFormat =
                phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            val timeZone =
                PhoneNumberToTimeZonesMapper.getInstance().getTimeZonesForNumber(phoneNumber)
                    .toString()
            val tz = TimeZone.getTimeZone(timeZone.substring(1, timeZone.length - 1))
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
            sdf.timeZone = tz
            val date = Date(System.currentTimeMillis())

            println(
                "testParsePhone $phone $iso ${Locale.getDefault().country} $phoneNationalFormat ${timeZone} ${
                    sdf.format(
                        date
                    )
                }"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun testSign() {
        val str = "123456"
        val md5 = EncryptUtils.md5(str)
        println(md5)
        println(str.reversed())
    }
}
