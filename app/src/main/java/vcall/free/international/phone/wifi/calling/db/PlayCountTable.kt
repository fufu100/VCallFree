package vcall.free.international.phone.wifi.calling.db

/**
 * Created by lyf on 2020/5/19.
 */
object PlayCountTable {
    val TB_NAME = "play_count"
    val ID = "_id"
    val COUNT = "count"
    val REWARD_COUNT = "reward_count"
    val CREDITS = "credits"
    val AD_CLICK_COUNT = "ad_click_count"
    val CLICK_COUNT_LIMIT_TIME = "click_count_limit_time"
    val LUCKY_CREDITS_CLICK_COUNT = "luck_credits_click_count"
    val DATE = "date"

    fun newCreateTableString(): String {
        val sb = StringBuilder()
        sb.append("create table if not exists ").append(TB_NAME).append(" (")
            .append(ID).append(" integer primary key autoincrement,")
            .append(COUNT).append(" integer,")
            .append(REWARD_COUNT).append(" integer,")
            .append(CREDITS).append(" integer,")
            .append(AD_CLICK_COUNT).append(" integer,")
            .append(CLICK_COUNT_LIMIT_TIME).append(" integer,")
            .append(LUCKY_CREDITS_CLICK_COUNT).append(" integer,")
            .append(DATE).append(" varchar")
            .append(")")
        return sb.toString()
    }

    fun newDeleteTableString(): String {
        return "DROP TABLE IF EXISTS $TB_NAME"
    }
}