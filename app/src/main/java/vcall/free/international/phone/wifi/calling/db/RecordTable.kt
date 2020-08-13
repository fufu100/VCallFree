package vcall.free.international.phone.wifi.calling.db

/**
 * Created by lyf on 2020/5/6.
 */
object RecordTable {
    val TB_NAME = "call_record"
    val ID = "_id"
    val PHONE = "phone"
    val ISO = "iso"
    val CODE = "code"
    val PREFIX = "prefix"
    val PHONE_ID = "phone_id"
    val CONTRACT_ID = "contract_id"
    val USERNAME = "user_name"
    val USER_PHOTO = "user_photo"
    val ADD_TIME = "add_time"
    val STATE = "state"
    val DURATION = "duration"
    val RATE = "rate"
    val COIN_COST = "coin_cost"


    fun newCreateTableString(): String {
        val sb = StringBuilder()
        sb.append("create table if not exists ").append(TB_NAME).append(" (")
            .append(ID).append(" integer primary key autoincrement,")
            .append(PHONE_ID).append(" integer,")
            .append(CONTRACT_ID).append(" integer,")
            .append(PHONE).append(" varchar,")
            .append(ISO).append(" varchar,")
            .append(CODE).append(" varchar,")
            .append(PREFIX).append(" varchar,")
            .append(USERNAME).append(" varchar,")
            .append(USER_PHOTO).append(" integer,")
            .append(STATE).append(" integer,")
            .append(DURATION).append(" integer,")
            .append(RATE).append(" integer,")
            .append(COIN_COST).append(" integer,")
            .append(ADD_TIME).append(" date")
            .append(")")
        return sb.toString()
    }

    fun newDeleteTableString(): String {
        return "DROP TABLE IF EXISTS $TB_NAME"
    }
}