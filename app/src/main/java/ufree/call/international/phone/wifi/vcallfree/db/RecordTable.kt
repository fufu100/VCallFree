package ufree.call.international.phone.wifi.vcallfree.db

/**
 * Created by lyf on 2020/5/6.
 */
object RecordTable {
    val TB_NAME = "call_record"
    val ID = "_id"
    val PHONE = "phone"
    val PHONE_ID = "phone_id"
    val CONTRACT_ID = "contract_id"
    val USERNAME = "user_name"
    val USER_PHOTO = "user_photo"
    val ADD_TIME = "add_time"
    val IS_RECEIVED = "is_received"
    val DURATION = "duration"


    fun newCreateTableString(): String {
        val sb = StringBuilder()
        sb.append("create table if not exists ").append(TB_NAME).append(" (")
            .append(ID).append(" integer primary key autoincrement,")
            .append(PHONE_ID).append(" integer,")
            .append(CONTRACT_ID).append(" integer,")
            .append(PHONE).append(" varchar,")
            .append(USERNAME).append(" varchar,")
            .append(USER_PHOTO).append(" integer,")
            .append(IS_RECEIVED).append(" integer,")
            .append(DURATION).append(" integer,")
            .append(ADD_TIME).append(" date")
            .append(")")
        return sb.toString()
    }

    fun newDeleteTableString(): String {
        return "DROP TABLE IF EXISTS $TB_NAME"
    }
}