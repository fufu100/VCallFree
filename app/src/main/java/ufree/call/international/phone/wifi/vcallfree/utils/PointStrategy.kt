package ufree.call.international.phone.wifi.vcallfree.utils

import kotlin.random.Random

/**
 * Created by lyf on 2020/5/17.
 */
class PointStrategy {

    companion object{
        val points = intArrayOf(0,5,10,20,50,100,200,500)
        val pointStrategy1 = intArrayOf(10,100,150,350,350,30,9,1)
        val pointStrategy2 = intArrayOf(10,50,100,250,450,130,9,1)
        val videoPoints = intArrayOf(10,20,50,100,200,500)
        val videoStrategy1 = intArrayOf(51,400,500,40,7,2)
        val videoStrategy2 = intArrayOf(42,200,520,200,35,3)
    }

    fun getRandom(probabilities:IntArray):Int{
        val random = Random.nextInt(0,1000)
        var t = 0
        for(i in  probabilities.indices){
            if(random >= t && random < t + probabilities[i]){
//                println("random return $i $random")
                return i
            }
            t += probabilities[i]
        }
        return 0
    }
}