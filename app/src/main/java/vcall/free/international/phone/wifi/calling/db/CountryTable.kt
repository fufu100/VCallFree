package vcall.free.international.phone.wifi.calling.db

/**
 * Created by lyf on 2020/5/7.
 */
object CountryTable {
    val TB_NAME = "country"
    val ID = "_id"
    val COUNTRY = "country"
    val ISO = "iso"
    val CODE = "code"
    val LENGTH = "length"
    val PREFIX = "prefix"


    fun newCreateTableString(): String {
        val sb = StringBuilder()
        sb.append("create table if not exists ").append(TB_NAME).append(" (")
            .append(ID).append(" integer primary key autoincrement,")
            .append(COUNTRY).append(" varchar,")
            .append(ISO).append(" varchar,")
            .append(CODE).append(" varchar,")
            .append(LENGTH).append(" integer,")
            .append(PREFIX).append(" varchar")
            .append(")")
        return sb.toString()
    }

    fun newDeleteTableString(): String {
        return "DROP TABLE IF EXISTS $TB_NAME"
    }
}