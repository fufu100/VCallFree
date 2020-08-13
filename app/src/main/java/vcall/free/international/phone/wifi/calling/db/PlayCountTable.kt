package vcall.free.international.phone.wifi.calling.db

/**
 * Created by lyf on 2020/5/19.
 */
object PlayCountTable {
    val TB_NAME = "play_count"
    val ID = "_id"
    val COUNT = "count"
    val DATE = "date"

    fun newCreateTableString(): String {
        val sb = StringBuilder()
        sb.append("create table if not exists ").append(TB_NAME).append(" (")
            .append(ID).append(" integer primary key autoincrement,")
            .append(COUNT).append(" integer,")
            .append(DATE).append(" varchar")
            .append(")")
        return sb.toString()
    }

    fun newDeleteTableString(): String {
        return "DROP TABLE IF EXISTS $TB_NAME"
    }
}