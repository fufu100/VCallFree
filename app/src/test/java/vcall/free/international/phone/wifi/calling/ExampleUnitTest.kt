package vcall.free.international.phone.wifi.calling

import com.google.i18n.phonenumbers.PhoneNumberToTimeZonesMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.Test

import org.junit.Assert.*
import vcall.free.international.phone.wifi.calling.utils.PointStrategy
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
        val date = Date()
        val c = Calendar.getInstance(Locale.US)
        val format = SimpleDateFormat("E,MM dd hh:mm a z",Locale.CHINA)
        println(format.format(Date(c.timeInMillis)))
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testStrategy(){
        val strategy = PointStrategy()
        val result = intArrayOf(0,0,0,0,0,0,0,0)
        val probabilities = intArrayOf(10,100,150,350,350,30,9,1)
        for(i in 0 until 100000){
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
    fun testParsePhone(){
        try {
            val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parseAndKeepRawInput("+12134883500", null)
            val iso = phoneNumberUtil.getRegionCodeForNumber(phoneNumber)
            val phone = phoneNumberUtil.getNationalSignificantNumber(phoneNumber)
            val phoneNationalFormat = phoneNumberUtil.format(phoneNumber,PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            val timeZone = PhoneNumberToTimeZonesMapper.getInstance().getTimeZonesForNumber(phoneNumber).toString()
            val tz = TimeZone.getTimeZone(timeZone.substring(1,timeZone.length - 1))
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
            sdf.timeZone = tz
            val date = Date(System.currentTimeMillis())

            println("testParsePhone $phone $iso ${Locale.getDefault().country} $phoneNationalFormat ${timeZone} ${sdf.format(date)}")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}
