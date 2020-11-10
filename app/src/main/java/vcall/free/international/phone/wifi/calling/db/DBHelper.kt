package com.newmotor.x5.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.api.Country
import vcall.free.international.phone.wifi.calling.api.Record
import vcall.free.international.phone.wifi.calling.db.CountryTable
import vcall.free.international.phone.wifi.calling.db.PlayCountTable
import vcall.free.international.phone.wifi.calling.db.RecordTable
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
        if(newVersion == 7){
            db?.execSQL("alter table ${PlayCountTable.TB_NAME} add column ${PlayCountTable.AD_CLICK_COUNT} integer")
        }

        println("DBHelper onUpgrade $oldVersion $newVersion")
    }

    companion object {
        private val DATABASE_NAME = "vcallfree_database.db"  //数据库名
        private val DATABASE_VERSION = 7    //数据库版本
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
                    cursor.getLong(cursor.getColumnIndex(RecordTable.PHONE_ID)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.CONTRACT_ID)),
                    cursor.getString(cursor.getColumnIndex(RecordTable.PHONE)),
                    cursor.getString(cursor.getColumnIndex(RecordTable.ISO)),
                    cursor.getString(cursor.getColumnIndex(RecordTable.CODE)),
                    cursor.getString(cursor.getColumnIndex(RecordTable.PREFIX)),
                    cursor.getString(cursor.getColumnIndex(RecordTable.USERNAME)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.USER_PHOTO)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.ADD_TIME)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.DURATION)),
                    cursor.getInt(cursor.getColumnIndex(RecordTable.RATE)),
                    cursor.getInt(cursor.getColumnIndex(RecordTable.COIN_COST)),
                    cursor.getInt(cursor.getColumnIndex(RecordTable.STATE))
                )
            )
        }

        return list
    }

    fun addCallRecord(record: Record) {
        println("DBHelper 存入通话记录：$record")
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
                    cursor.getString(cursor.getColumnIndex(CountryTable.COUNTRY)),
                    cursor.getString(cursor.getColumnIndex(CountryTable.ISO)),
                    cursor.getString(cursor.getColumnIndex(CountryTable.CODE)),
                    cursor.getInt(cursor.getColumnIndex(CountryTable.LENGTH)),
                    cursor.getString(cursor.getColumnIndex(CountryTable.PREFIX))
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
                    cursor.getString(cursor.getColumnIndex(CountryTable.ISO)),
                    cursor.getString(cursor.getColumnIndex(CountryTable.CODE)),
                    cursor.getString(cursor.getColumnIndex(CountryTable.CODE)),
                    cursor.getString(cursor.getColumnIndex(CountryTable.COUNTRY))
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
                        cursor.getString(cursor.getColumnIndex(CountryTable.COUNTRY)),
                        cursor.getString(cursor.getColumnIndex(CountryTable.ISO)),
                        cursor.getString(cursor.getColumnIndex(CountryTable.CODE)),
                        cursor.getInt(cursor.getColumnIndex(CountryTable.LENGTH)),
                        cursor.getString(cursor.getColumnIndex(CountryTable.PREFIX))
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
                cursor.getString(cursor.getColumnIndex(CountryTable.COUNTRY)),
                cursor.getString(cursor.getColumnIndex(CountryTable.ISO)),
                cursor.getString(cursor.getColumnIndex(CountryTable.CODE)),
                cursor.getInt(cursor.getColumnIndex(CountryTable.LENGTH)),
                cursor.getString(cursor.getColumnIndex(CountryTable.PREFIX))
            )
        }
        return null
    }

    fun getCountryByCode(code:String):Country?{
        val cursor = queryAndAll(CountryTable.TB_NAME,CountryTable.CODE,code)
        if(cursor?.moveToNext() == true){
            return Country(
                cursor.getString(cursor.getColumnIndex(CountryTable.COUNTRY)),
                cursor.getString(cursor.getColumnIndex(CountryTable.ISO)),
                cursor.getString(cursor.getColumnIndex(CountryTable.CODE)),
                cursor.getInt(cursor.getColumnIndex(CountryTable.LENGTH)),
                cursor.getString(cursor.getColumnIndex(CountryTable.PREFIX))
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
            return cursor.getInt(cursor.getColumnIndex(PlayCountTable.COUNT))
        }
        return 0
    }

    fun getTodayCredits():Int{
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        val date = format.format(Date())
        println("getTodayCredits date=$date")
        val cursor = queryAndAll(PlayCountTable.TB_NAME,PlayCountTable.DATE,date)
        if(cursor?.moveToNext() == true){
            return cursor.getInt(cursor.getColumnIndex(PlayCountTable.CREDITS))
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
        println("getTodayCredits date=$date")
        val cursor = queryAndAll(PlayCountTable.TB_NAME,PlayCountTable.DATE,date)
        if(cursor?.moveToNext() == true){
            return cursor.getInt(cursor.getColumnIndex(PlayCountTable.AD_CLICK_COUNT))
        }
        return 0
    }

    fun addAdClickCount(){
        val count = getAdClickCount()
        val cv = ContentValues()
        val format = SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)
        cv.put(PlayCountTable.AD_CLICK_COUNT,count + 1)
        cv.put(PlayCountTable.DATE,format.format(Date()))
        updateOrInsert(PlayCountTable.TB_NAME,cv,PlayCountTable.DATE)
    }
}