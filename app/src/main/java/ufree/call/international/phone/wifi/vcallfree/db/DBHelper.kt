package com.newmotor.x5.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.translate.english.voice.lib.App
import ufree.call.international.phone.wifi.vcallfree.api.Country
import ufree.call.international.phone.wifi.vcallfree.api.Record
import ufree.call.international.phone.wifi.vcallfree.db.CountryTable
import ufree.call.international.phone.wifi.vcallfree.db.RecordTable

/**
 * Created by lyf on 2019-11-29.
 */

class DBHelper : CommonDB(App.context!!, DATABASE_NAME, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(RecordTable.newCreateTableString())
        db?.execSQL(CountryTable.newCreateTableString())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(RecordTable.newDeleteTableString())
        db?.execSQL(RecordTable.newCreateTableString())

        db?.execSQL(CountryTable.newDeleteTableString())
        db?.execSQL(CountryTable.newCreateTableString())
    }

    companion object {
        private val DATABASE_NAME = "vcallfree_database.db"  //数据库名
        private val DATABASE_VERSION = 1    //数据库版本
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
        val cursor = queryAll(RecordTable.TB_NAME)
        val list = mutableListOf<Record>()
        while (cursor.moveToNext()) {
            list.add(
                Record(
                    cursor.getLong(cursor.getColumnIndex(RecordTable.PHONE_ID)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.CONTRACT_ID)),
                    cursor.getString(cursor.getColumnIndex(RecordTable.PHONE)),
                    cursor.getString(cursor.getColumnIndex(RecordTable.USERNAME)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.USER_PHOTO)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.ADD_TIME)),
                    cursor.getLong(cursor.getColumnIndex(RecordTable.DURATION)),
                    cursor.getInt(cursor.getColumnIndex(RecordTable.IS_RECEIVED)) == 1
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
        cv.put(RecordTable.PHONE_ID, record.phoneId)
        cv.put(RecordTable.CONTRACT_ID, record.contractId)
        cv.put(RecordTable.ADD_TIME, System.currentTimeMillis())
        cv.put(RecordTable.DURATION, record.duration)
        cv.put(RecordTable.IS_RECEIVED, if (record.isReceived) 1 else 0)
        insert(RecordTable.TB_NAME, cv)
    }


    fun getAllCountries(): MutableList<Country> {
        val cursor = queryAll(CountryTable.TB_NAME)
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
}