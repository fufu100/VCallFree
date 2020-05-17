package ufree.call.international.phone.wifi.vcallfree

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test

import org.junit.Assert.*
import ufree.call.international.phone.wifi.vcallfree.utils.PointStrategy

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
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
}
