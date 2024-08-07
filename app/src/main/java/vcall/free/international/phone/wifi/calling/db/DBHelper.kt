package vcall.free.international.phone.wifi.calling.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.api.Country
import vcall.free.international.phone.wifi.calling.api.Record
import vcall.free.international.phone.wifi.calling.utils.toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lyf on 2019-11-29.
 */

class DBHelper : CommonDB(App.context!!, DATABASE_NAME, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(RecordTable.newCreateTableString())
        db?.execSQL(CountryTable.newCreateTableString())
        db?.execSQL(PlayCountTable.newCreateTableString())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(RecordTable.newDeleteTableString())
        db?.execSQL(RecordTable.newCreateTableString())

        db?.execSQL(CountryTable.newDeleteTableString())
        db?.execSQL(CountryTable.newCreateTableString())

//        db?.execSQL(PlayCountTable.newDeleteTableString())
//        db?.execSQL(PlayCountTable.newCreateTableString())
        if(newVersion == 7 ){
            db?.execSQL("alter table ${PlayCountTable.TB_NAME} add column ${PlayCountTable.AD_CLICK_COUNT} integer")
        }else if(newVersion == 8){
            db?.execSQL("alter table ${PlayCountTable.TB_NAME} add column ${PlayCountTable.CLICK_COUNT_LIMIT_TIME} integer")
        }else if(newVersion == 9){
            db?.execSQL("alter table ${PlayCountTable.TB_NAME} add column ${PlayCountTable.LUCKY_CREDITS_CLICK_COUNT} integer")
        }else if(newVersion == 10){
            db?.execSQL("alter table ${PlayCountTable.TB_NAME} add column ${PlayCountTable.REWARD_COUNT} integer")
        }

        println("DBHelper onUpgrade $oldVersion $newVersion")
    }

    companion object {
        private val DATABASE_NAME = "vcallfree_database.db"  //数据库名
        private val DATABASE_VERSION = 10    //数据库版本
        var instance: DBHelper? = null
            get() {
                if (field == null) {
                    field = DBHelper()
                }
                return field
            }

        fun get(): DBHelper {
            return instance!!
        }
    }

    init {
        open()
    }

    fun getCallRecords(): MutableList<Record> {
        val cursor = queryAll(RecordTable.TB_NAME,RecordTable.ADD_TIME + " desc")
        val list = mutableListOf<Record>()
        while (cursor.moveToNext()) {
            list.add(
                Record(
                    cursor.getLong(cursor.getColumnIndexOrThrow(RecordTable.ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(RecordTable.PHONE_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(RecordTable.CONTRACT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(RecordTable.PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(RecordTable.ISO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(RecordTable.CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(RecordTable.PREFIX)),
                    cursor.getString(cursor.getColumnIndexOrThrow(RecordTable.USERNAME)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(RecordTable.USER_PHOTO)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(RecordTable.ADD_TIME)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(RecordTable.DURATION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(RecordTable.RATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(RecordTable.COIN_COST)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(RecordTable.STATE))
                )
            )
        }

        return list
    }

    fun addCallRecord(record: Record?) {
        println("DBHelper 存入通话记录：$record")
        if(record == null){
            return
        }
        val cv = ContentValues()
        cv.put(RecordTable.USERNAME, record.username)
        cv.put(RecordTable.USER_PHOTO, record.userPhoto)
        cv.put(RecordTable.PHONE, record.phone)
        cv.put(RecordTable.ISO, record.iso)
        cv.put(RecordTable.CODE, record.code)
        cv.put(RecordTable.PREFIX, record.prefix)
        cv.put(RecordTable.PHONE_ID, record.phoneId)
        cv.put(RecordTable.CONTRACT_ID, record.contractId)
        cv.put(RecordTable.ADD_TIME, System.currentTimeMillis())
        cv.put(RecordTable.DURATION, record.duration)
        cv.put(RecordTable.RATE, record.rate)
        cv.put(RecordTable.COIN_COST, record.coinCost)
        cv.put(RecordTable.STATE, record.state)
        insert(RecordTable.TB_NAME, cv)
    }

    fun deleteCallRecord(id:Long){
        deleteAll(RecordTable.TB_NAME,RecordTable.ID,id.toString())
    }


    fun getAllCountries(keyword:String = ""): MutableList<Country> {
        val cursor:Cursor
        if(keyword.isEmpty()){
            cursor = queryAll(CountryTable.TB_NAME)
        }else {
            cursor = queryLike(CountryTable.TB_NAME, null, CountryTable.COUNTRY, keyword)
        }
        val list = mutableListOf<Country>()
        while (cursor.moveToNext()) {
            list.add(
                Country(
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.COUNTRY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.ISO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.CODE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(CountryTable.LENGTH)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.PREFIX))
                )
            )
        }
        return list
    }

    fun getAllCountries2(keyword:String = ""): MutableList<Array<String>> {
        val cursor:Cursor
        if(keyword.isEmpty()){
            cursor = queryAll(CountryTable.TB_NAME)
        }else {
            cursor = queryLike(CountryTable.TB_NAME, null, CountryTable.COUNTRY, keyword)
        }
        val list = mutableListOf<Array<String>>()
        while (cursor.moveToNext()) {
            list.add(
                arrayOf(
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.ISO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.COUNTRY))
                )
            )
        }
        return list
    }

    fun getCountriesByISOs(isos:Array<String>?):MutableList<Country> {
        val list = mutableListOf<Country>()
        if(isos != null) {
            val cursor = queryInAll(CountryTable.TB_NAME,CountryTable.ISO,isos,null)
            while (cursor.moveToNext()) {
                list.add(
                    Country(
                        cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.COUNTRY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.ISO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.CODE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(CountryTable.LENGTH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.PREFIX))
                    )
                )
            }
        }
        return list
    }

    fun getCountry(iso:String):Country?{
        val cursor = queryAndAll(CountryTable.TB_NAME,CountryTable.ISO,iso)
        if(cursor?.moveToNext() == true){
            return Country(
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.COUNTRY)),
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.ISO)),
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.CODE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(CountryTable.LENGTH)),
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.PREFIX))
            )
        }
        return null
    }

    fun getCountryByCode(code:String):Country?{
        val cursor = queryAndAll(CountryTable.TB_NAME,CountryTable.CODE,code)
        if(cursor?.moveToNext() == true){
            return Country(
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.COUNTRY)),
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.ISO)),
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.CODE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(CountryTable.LENGTH)),
                cursor.getString(cursor.getColumnIndexOrThrow(CountryTable.PREFIX))
            )
        }
        return null
    }

    fun getCountryCount():Int{
        return count(CountryTable.TB_NAME)
    }

    fun addCountry(country: Country) {
        val cv = ContentValues()
        cv.put(CountryTable.COUNTRY, country.country)
        cv.put(CountryTable.ISO, country.iso)
        cv.put(CountryTable.CODE, country.code)
        cv.put(CountryTable.LENGTH, country.length)
        cv.put(CountryTable.PREFIX, country.prefix)
        insert(CountryTable.TB_NAME, cv)
    }

    fun updateCountry(country: Country){
        val cv = ContentValues()
        cv.put(CountryTable.COUNTRY, country.country)
        cv.put(CountryTable.ISO, country.iso)
        cv.put(CountryTable.CODE, country.code)
        cv.put(CountryTable.LENGTH, country.length)
        cv.put(CountryTable.PREFIX, country.prefix)
        updateOrInsert(CountryTable.TB_NAME,cv,CountryTable.ISO)
    }

    fun addPlayCount(count:Int){
        val cv = ContentValues()
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        cv.put(PlayCountTable.COUNT,count)
        cv.put(PlayCountTable.DATE,format.format(Date()))
        updateOrInsert(PlayCountTable.TB_NAME,cv,PlayCountTable.DATE)
    }
    fun getPlayCount():Int{
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        val date = format.format(Date())
        println("getPlayCount date=$date")
        val cursor = queryAndAll(PlayCountTable.TB_NAME,PlayCountTable.DATE,date)
        if(cursor?.moveToNext() == true){
            return cursor.getInt(cursor.getColumnIndexOrThrow(PlayCountTable.COUNT))
        }
        return 0
    }

    fun addRewardCount(count:Int){
        val cv = ContentValues()
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        cv.put(PlayCountTable.REWARD_COUNT,count)
        cv.put(PlayCountTable.DATE,format.format(Date()))
        updateOrInsert(PlayCountTable.TB_NAME,cv,PlayCountTable.DATE)
    }
    fun getRewardCount():Int{
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        val date = format.format(Date())
        println("getPlayCount date=$date")
        val cursor = queryAndAll(PlayCountTable.TB_NAME,PlayCountTable.DATE,date)
        if(cursor?.moveToNext() == true){
            return cursor.getInt(cursor.getColumnIndexOrThrow(PlayCountTable.REWARD_COUNT))
        }
        return 0
    }

    fun getTodayCredits():Int{
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        val date = format.format(Date())
        println("getTodayCredits date=$date")
        val cursor = queryAndAll(PlayCountTable.TB_NAME,PlayCountTable.DATE,date)
        if(cursor?.moveToNext() == true){
            return cursor.getInt(cursor.getColumnIndexOrThrow(PlayCountTable.CREDITS))
        }
        return 0
    }

    fun setTodayCredits(credits:Int){
        val cv = ContentValues()
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        cv.put(PlayCountTable.CREDITS,credits)
        cv.put(PlayCountTable.DATE,format.format(Date()))
        updateOrInsert(PlayCountTable.TB_NAME,cv,PlayCountTable.DATE)
    }

    fun getAdClickCount():Int{
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        val date = format.format(Date())
        println("getAdClickCount date=$date")
        val cursor = queryAndAll(PlayCountTable.TB_NAME,PlayCountTable.DATE,date)
        if(cursor?.moveToNext() == true){
            val count = cursor.getInt(cursor.getColumnIndexOrThrow(PlayCountTable.AD_CLICK_COUNT))
            val t = cursor.getLong(cursor.getColumnIndexOrThrow(PlayCountTable.CLICK_COUNT_LIMIT_TIME))
            if(count > 15 && System.currentTimeMillis() - t > 3600 * 1000){
                val cv = ContentValues()
                cv.put(PlayCountTable.AD_CLICK_COUNT,0)
                cv.put(PlayCountTable.CLICK_COUNT_LIMIT_TIME,System.currentTimeMillis())
                cv.put(PlayCountTable.DATE,format.format(Date()))
                updateOrInsert(PlayCountTable.TB_NAME,cv,PlayCountTable.DATE)
                return 0
            }else{
                return count
            }

        }
        return 0
    }

    fun addAdClickCount(){
        val count = getAdClickCount()
        if(count + 1 == 15){
            App.context?.toast(R.string.tip_ad_click_limit)
        }
        val cv = ContentValues()
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        cv.put(PlayCountTable.AD_CLICK_COUNT,count + 1)
        cv.put(PlayCountTable.CLICK_COUNT_LIMIT_TIME,System.currentTimeMillis())
        cv.put(PlayCountTable.DATE,format.format(Date()))
        updateOrInsert(PlayCountTable.TB_NAME,cv,PlayCountTable.DATE)
    }

    fun getLuckyCreditsClickCount():Int{
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        val date = format.format(Date())
        println("getLuckyCreditsClickCount date=$date")
        val cursor = queryAndAll(PlayCountTable.TB_NAME,PlayCountTable.DATE,date)
        if(cursor?.moveToNext() == true){
            val count = cursor.getInt(cursor.getColumnIndexOrThrow(PlayCountTable.LUCKY_CREDITS_CLICK_COUNT))
            return count
        }
        return 0
    }


    fun addLuckyCreditsClickCount(){
        val count = getLuckyCreditsClickCount()
        val cv = ContentValues()
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        cv.put(PlayCountTable.LUCKY_CREDITS_CLICK_COUNT,count + 1)
        cv.put(PlayCountTable.DATE,format.format(Date()))
        updateOrInsert(PlayCountTable.TB_NAME,cv,PlayCountTable.DATE)
    }
}