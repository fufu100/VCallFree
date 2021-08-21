package vcall.free.international.phone.wifi.calling.utils

import kotlin.random.Random

/**
 * Created by lyf on 2020/5/17.
 */
class PointStrategy {

    companion object{
        val points = intArrayOf(5,10,20,50,100,200,5,500)
        val pointStrategy1 = intArrayOf(850,100,30,10,10,0,0,0)
        val pointStrategy2 = intArrayOf(650,220,100,20,10,0,0,0)
        val pointStrategy3 = intArrayOf(400,450,100,30,10,10,0,0)
        val pointStrategy4 = intArrayOf(350,350,200,60,20,20,0,0)
        val pointStrategy5 = intArrayOf(100,250,400,200,30,20,0,0)
        val pointStrategy6 = intArrayOf(100,200,250,370,50,20,0,10)
        val pointStrategy7 = intArrayOf(50,100,260,400,150,30,0,10)
        val pointStrategy8 = intArrayOf(30,120,200,400,140,80,0,30)
        val pointStrategy9 = intArrayOf(60,70,80,200,400,160,0,30)
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