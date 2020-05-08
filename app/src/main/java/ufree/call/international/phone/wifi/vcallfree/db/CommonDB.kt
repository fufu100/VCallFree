package com.newmotor.x5.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import android.util.Log
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by lyf on 2019-11-29.
 */
abstract class CommonDB(context: Context, name:String, version:Int) :SQLiteOpenHelper(context,name,null,version){

    companion object{
        val QUERY_WHERE_LIKE = " like ? "  //like关键字
        val QUERY_WHERE_OR = " or "         //or关键字
        val QUERY_WHERE_AND = " and "      //and关键字
        val QUERY_WHERE_PARAM = " = ? "      //参数
        val QUERY_WHERE_GREATER_EQUAL = " >= ? "      //大于等于
        val QUERY_WHERE_GREATER = " > ? "      //大于
        val QUERY_WHERE_LESS_EQUAL = " <= ? "      //小于等于
        val QUERY_WHERE_LESS = " < ? "      //小于
        val QUERY_LEFT_BRACKET = " ( "      //左括号
        val QUERY_RIGHT_BRACKET = " ) "      //右括号
        val FD_ID = "_id"   //流水号id字段
    }

    private val ID = arrayOf(FD_ID)
    private val WHERE_ID = FD_ID + QUERY_WHERE_PARAM

    protected lateinit var db: SQLiteDatabase


    /*
	 * 打开数据库
	 */
    protected fun open() {
        if (!::db.isInitialized || !db.isOpen) {
            db = writableDatabase
        }
        if (!::db.isInitialized) {
            throw NullPointerException("db is null")
        }
    }

    /**
     * 关闭数据库
     */
    override fun close() {
        if (!::db.isInitialized) {
            db.close()
        }
    }

    /**
     * exec sql
     * @param sql
     * @return void
     * @throws
     */
    fun execSQL(sql: String) {
        open()
        db.execSQL(sql)
    }

    /**
     * 判断某个表是否存在
     * @param tabName
     * @return
     * @return boolean
     * @throws
     */
    fun tabIsExist(tabName: String?): Boolean {
        var result = false
        if (tabName == null) {
            return false
        }
        open()
        var cursor: Cursor? = null
        try {
            db = this.readableDatabase
            val sql =
                "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim { it <= ' ' } + "' "
            cursor = db.rawQuery(sql, null)
            if (cursor!!.moveToNext()) {
                val count = cursor.getInt(0)
                if (count > 0) {
                    result = true
                }
            }

        } catch (e: Exception) {
        }

        return result
    }

    /**
     * exec sql with args
     * @param sql
     * @param args
     * @return void
     * @throws
     */
    fun execSQL(sql: String, args: Array<String>) {
        open()
        db.execSQL(sql, args)
    }

    fun beginTransaction() {
        open()
        db.beginTransaction()
    }

    fun endTransaction() {
        db.endTransaction()
    }

    fun setTransactionSuccessful() {
        db.setTransactionSuccessful()
    }

    /**
     * 删除表格所有数据
     * @param table
     */
    fun deleteAll(table: String) {
        open()
        db.delete(table, null, null)
    }

    /**
     * 删除指定的记录
     * @param table  表名
     * @param field 字段名
     * @param value 匹配值
     */
    fun deleteAll(table: String, field: String, value: String): Int? {
        return deleteAll(table, arrayOf(field), arrayOf(value))
    }

    /**
     * 删除指定的记录
     * @param table  表名
     * @param fields 字段名
     * @param values 匹配值
     */
    fun deleteAll(table: String, fields: Array<String>?, values: Array<String>): Int {
        open()
        if (fields == null || fields.size == 0) {
            return db!!.delete(table, null, null)
        } else {
            var where = ""
            for (i in fields.indices) {
                if (i > 0) where += QUERY_WHERE_AND
                where += fields[i] + QUERY_WHERE_PARAM
            }
            return db!!.delete(table, where, values)
        }
    }

    /**
     * 删除小于等于某个值的记录
     * @param table  表名
     * @param field  字段
     * @param value  值
     */
    fun deleteLess(table: String, field: String, value: Any) {
        open()
        db.delete(table, "$field <= ?", arrayOf(value.toString()))
    }

    /**
     * 删除大于某个值的记录
     * @param table  表名
     * @param field  字段
     * @param value  值
     */
    fun deleteGreater(table: String, field: String, value: Any) {
        open()
        db.delete(table, "$field > ?", arrayOf(value.toString()))
    }

    /**
     * 更新数据库
     * @param table
     * @param values
     * @param field
     * @param value
     */
    fun update(table: String, values: ContentValues, field: String, value: String) {
        open()
        if (!TextUtils.isEmpty(field) && !TextUtils.isEmpty(value))
            db.update(table, values, field + QUERY_WHERE_PARAM, arrayOf(value))
        else
            db.update(table, values, null, null)
    }

    /**
     * 更新数据库
     * @param table
     * @param values
     * @param field
     * @param value
     */
    fun updateGreater(table: String, values: ContentValues, field: String, value: String) {
        open()
        if (!TextUtils.isEmpty(field) && !TextUtils.isEmpty(value))
            db!!.update(table, values, "$field >= ?", arrayOf(value))
        else
            db!!.update(table, values, null, null)
    }

    /**
     * 更新入数据)
     * @param table  表名
     * @param values  值
     * @param onlyField  判断记录唯一的字段名
     */
    fun update(table: String, values: ArrayList<ContentValues>?, onlyField: String) {
        if (values != null && values.size > 0) {
            val cursor: Cursor? = null
            open()
            try {
                db = writableDatabase
                for (value in values) {
                    db!!.update(table, value, "$onlyField = ?", arrayOf(value.getAsString(onlyField)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
    }

    /**
     * 更新数据
     * @param table  表名
     * @param values  值
     * @param onlyField  判断记录唯一的字段名
     */
    fun update(table: String, values: ContentValues, onlyField: String) {
        open()
        val cursor: Cursor? = null
        try {
            db = writableDatabase
            db!!.update(table, values, "$onlyField = ?", arrayOf(values.getAsString(onlyField)))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    /**
     * 更新或插入数据(如果存在则更新,否则插入)
     * @param table  表名
     * @param values  值
     * @param onlyField  判断记录唯一的字段名
     */
    fun updateOrInsert(table: String, values: ContentValues, onlyField: String) {
        open()
        var cursor: Cursor? = null
        try {
            db = writableDatabase
            if (values.containsKey(onlyField)) {
                cursor = db.query(
                    table,
                    ID,
                    onlyField + QUERY_WHERE_PARAM,
                    arrayOf(values.getAsString(onlyField)),
                    null,
                    null,
                    null
                )
                if (cursor != null && cursor.count == 1) {
                    cursor.moveToFirst()
                    db.update(table, values, WHERE_ID, arrayOf(cursor.getInt(0).toString()))
                } else {
                    db.insert(table, null, values)
                }
            } else {
                db.insert(table, null, values)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    /**
     * 更新或插入数据(如果存在则更新,否则插入)
     * @param table  表名
     * @param values  值
     * @param onlyField  判断记录唯一的字段名
     */
    fun updateOrInsert(table: String, values: List<ContentValues>?, onlyField: String) {
        if (values != null && values.size > 0) {
            open()
            var cursor: Cursor? = null
            try {
                db = writableDatabase
                val value = Array(values.size){""}
                val len = values.size
                for (i in 0 until len) {
                    value[i] = values[i].getAsString(onlyField)
                }
                cursor = queryInAll(db, table, arrayOf(FD_ID, onlyField), onlyField, value, null)
                var keyValues: HashMap<String, String>? = null
                if (cursor != null && cursor.count > 0) {
                    keyValues = HashMap(18)
                    while (cursor.moveToNext()) {
                        keyValues[cursor.getString(1)] = cursor.getString(0)
                    }
                }
                var va: String = ""
                if (null == keyValues || keyValues.size == 0) {
                    for (v in values) {
                        db.insert(table, null, v)
                    }
                } else {
                    for (v in values) {
                        va = keyValues[v.get(onlyField)]!!
                        if (TextUtils.isEmpty(va)) {
                            db.insert(table, null, v)
                        } else {
                            db.update(table, v, WHERE_ID, arrayOf<String>(va))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
    }


    /**
     * 查看某个表是否有数据
     * @param table
     * @return
     */
    fun hasData(table: String): Boolean {
        val count = count(table, null, null, false)
        return count > 0
    }

    /**
     * 查询表table最后一条记录的field字段的值
     * @param table
     * @param field
     * @return
     */
    fun queryLast(table: String, field: String): String? {
        var r: String? = null
        var c: Cursor? = null
        try {
            c = queryLast(table, arrayOf(field))
            if (c != null) {
                c.moveToFirst()
                r = c.getString(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
        }
        return r
    }

    /**
     * 查询表table最后一条记录的fields字段的值
     * @param table
     * @param coloumns
     * @return
     */
    fun queryLast(table: String, coloumns: Array<String>): Cursor? {
        open()
        val c = db.query(table, coloumns, null, null, null, null, FD_ID, "1")
        return if (c != null && c.count > 0) {
            c
        } else null
    }


    /**
     * 向表格插入一条记录
     * @param table  表名
     * @param values 值
     */
    fun insert(table: String, values: ContentValues): Long {
        open()
        return db.insert(table, null, values)
    }

    /**
     * 支持事务的多条数据插入
     * @param table
     * @param values
     * @return
     */
    fun insert(table: String, values: ArrayList<ContentValues>?): Int {
        if (values != null && values.size > 0) {
            open()
            db.beginTransaction()
            try {
                for (value in values) {
                    if (db.insert(table, null, value) < 0) {
                        return 0
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return values.size
        }
        return 0
    }

    /**
     * 执行sql语句
     * @param sql
     * @param args
     * @return
     */
    fun rawQuery(sql: String, args: Array<String>): Cursor {
        open()
        return db.rawQuery(sql, args)
    }

    /**
     * 获取数量
     * @param table
     * @param andFields
     * @param orFileds
     * @param removeRepeat
     */
    fun count(
        table: String,
        andFields: Array<String>?,
        andValues: Array<String>?,
        orFileds: Array<String>,
        orValues: Array<String>?,
        removeRepeat: Boolean
    ): Int {
        var cursor: Cursor? = null
        open()
        try {
            val buffer = StringBuffer(" select  ")
            if (removeRepeat) {
                buffer.append(" distinct ")
            }
            buffer.append(" count(*) from $table")
            var where = ""
            if (andFields != null && andValues != null) {
                where += " where "
                for (i in andFields.indices) {
                    if (i > 0) where += QUERY_WHERE_AND
                    where += " " + andFields[i] + "=? "
                }
            }
            if (orValues != null && orValues != null) {
                var isEmpty = false
                if (TextUtils.isEmpty(where)) {
                    where += " where "
                    isEmpty = true
                } else {
                    where += "$QUERY_WHERE_AND ("
                }
                for (i in orFileds.indices) {
                    if (i > 0) where += QUERY_WHERE_OR
                    where += " " + orFileds[i] + "=? "
                }
                if (!isEmpty) where += " )"
            }
            buffer.append(where)
            var values: Array<String>? = null
            if (orValues == null) {
                values = andValues
            } else if (andValues == null) {
                values = orValues
            } else {
                values = Array(andValues.size + orValues.size){""}
                System.arraycopy(andValues, 0, values, 0, andValues.size)
                System.arraycopy(orValues, 0, values, andValues.size, orValues.size)
            }
            cursor = db.rawQuery(buffer.toString(), values)
            if (cursor == null) {
                return 0
            }
            cursor.moveToFirst()
            return cursor.getInt(0)
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        } finally {
            cursor?.close()
        }
    }

    /**
     * 获取数量
     * @param table
     * @param fields
     * @param values
     * @param removeRepeat
     */
    fun count(
        table: String,
        fields: Array<String>?,
        values: Array<String>?,
        removeRepeat: Boolean
    ): Int {
        var cursor: Cursor? = null
        open()
        try {
            val buffer = StringBuffer(" select  ")
            if (removeRepeat) {
                buffer.append(" distinct ")
            }
            buffer.append(" count(*) from $table")
            var where = ""
            if (fields != null && values != null) {
                where += " where "
                for (i in fields.indices) {
                    if (i > 0) where += QUERY_WHERE_AND
                    where += " " + fields[i] + "=? "
                }
            }
            buffer.append(where)
            cursor = db.rawQuery(buffer.toString(), values)
            if (cursor == null) {
                return 0
            }
            cursor.moveToFirst()
            return cursor.getInt(0)
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        } finally {
            cursor?.close()
        }
    }

    /**
     * 获取数量
     * @param table
     */
    fun count(table: String): Int {
        var cursor: Cursor? = null
        open()
        try {
            val buffer = StringBuffer(" select  ")
            buffer.append(" count(*) from $table")
            cursor = db.rawQuery(buffer.toString(), null)
            if (cursor == null) {
                return 0
            }
            cursor.moveToFirst()
            return cursor.getInt(0)
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        } finally {
            cursor?.close()
        }
    }

    /**
     * 求某表中某个字段的最大值(该字段的类型为整型)
     * @param table
     * @param field
     * @return
     */
    fun max(table: String, field: String): Int {
        open()
        var c: Cursor? = null
        try {
            c = db.query(table, arrayOf("max($field)"), null, null, null, null, null)
            if (c != null && c.count > 0) {
                c.moveToFirst()
                return c.getInt(0)
            }
        } catch (e: Exception) {
        } finally {
            c?.close()
        }
        return 0
    }

    /**
     * 求某表中某个字段的最小值(该字段的类型为整型)
     * @param table
     * @param field
     * @return
     */
    fun min(table: String, field: String): Int {
        open()
        var c: Cursor? = null
        try {
            c = db.query(table, arrayOf("min($field)"), null, null, null, null, null)
            if (c != null && c.count > 0) {
                c.moveToFirst()
                return c.getInt(0)
            }
        } catch (e: Exception) {
        } finally {
            c?.close()
        }
        return 0
    }

    /**
     * 查询表的所有数据
     * @param table 表名
     * @return
     */
    fun queryAll(table: String): Cursor {
        return queryAll(table, FD_ID)
    }

    /**
     * 查询表的所有数据
     * @param table 表名
     * @return
     */
    fun queryNot(table: String, filed: String, value: Any): Cursor? {
        return queryNot(table, filed, value, FD_ID)
    }

    /**
     * 查询表的所有数据
     * @param table 表名
     * @return
     */
    fun queryNot(table: String, filed: String, value: Any, orderBy: String): Cursor? {
        open()
        val c = db.query(table, null, "$filed != ?", arrayOf(value.toString()), null, null, orderBy)
        c?.moveToFirst()
        return c
    }

    /**
     * 查询表的所有数据
     * @param table 表名
     * @return
     */
    fun queryAll(table: String, orderBy: String): Cursor {
        open()
        return db.query(table, null, null, null, null, null, orderBy)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param curpage 当前页
     * @param pagesize 每页的大小
     * @return
     */
    fun queryAll(table: String, curpage: Int, pagesize: Int): Cursor? {
        return queryAllRaw(table, (curpage - 1) * pagesize, pagesize, FD_ID)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param curpage 当前页
     * @param pagesize 每页的大小
     * @param order 排序
     * @return
     */
    fun queryAll(table: String, curpage: Int, pagesize: Int, order: String): Cursor? {
        return queryAllRaw(table, (curpage - 1) * pagesize, pagesize, order)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param beginRow 开始行
     * @param pagesize 每页的大小
     * @param order 排序
     * @return
     */
    fun queryAllRaw(table: String, beginRow: Int, pagesize: Int, order: String): Cursor? {
        return queryAllRaw(table, null, null, beginRow, pagesize, order)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param curpage 当前页
     * @param pagesize 每页的大小
     * @return
     */
    fun queryAll(table: String, field: String, value: Any, curpage: Int, pagesize: Int): Cursor? {
        return queryAllRaw(table, field, value, (curpage - 1) * pagesize, pagesize, FD_ID)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param curpage 当前页
     * @param pagesize 每页的大小
     * @param order 排序
     * @return
     */
    fun queryAll(
        table: String,
        field: String,
        value: Any,
        curpage: Int,
        pagesize: Int,
        order: String
    ): Cursor? {
        return queryAllRaw(table, field, value, (curpage - 1) * pagesize, pagesize, order)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param beginRow 开始行
     * @param pagesize 每页的大小
     * @param order 排序
     * @return
     */
    fun queryAllRaw(
        table: String,
        field: String?,
        value: Any?,
        beginRow: Int,
        pagesize: Int,
        order: String
    ): Cursor? {
        return if (field == null) {
            queryRaw(table, null, null, beginRow, pagesize, order)
        } else queryRaw(
            table,
            arrayOf(field),
            arrayOf(value!!.toString()),
            beginRow,
            pagesize,
            order
        )
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param curpage 当前页
     * @param pagesize 每页的大小
     * @return
     */
    fun queryAll(
        table: String,
        fields: Array<String>,
        values: Array<String>,
        curpage: Int,
        pagesize: Int
    ): Cursor? {
        return queryRaw(table, fields, values, (curpage - 1) * pagesize, pagesize, FD_ID)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param curpage 当前页
     * @param pagesize 每页的大小
     * @param order 排序
     * @return
     */
    fun queryAll(
        table: String,
        fields: Array<String>,
        values: Array<String>,
        curpage: Int,
        pagesize: Int,
        order: String
    ): Cursor? {
        return queryRaw(table, fields, values, (curpage - 1) * pagesize, pagesize, order)
    }

    /**
     * 分页查询表的所有数据
     * @param table 表名
     * @param beginRow 当前页
     * @param pagesize 每页的大小
     * @param order 排序
     * @return
     */
    fun queryRaw(
        table: String,
        fields: Array<String>?,
        values: Array<String>?,
        beginRow: Int,
        pagesize: Int,
        order: String
    ): Cursor? {
        open()
        val limit = "$beginRow,$pagesize"
        var c: Cursor? = null
        if (fields == null || fields.size == 0) {
            c = db.query(table, null, null, null, null, null, order, limit)
        } else {
            var where = ""
            for (i in fields.indices) {
                if (i > 0) where += QUERY_WHERE_AND
                where += " " + fields[i] + " = ? "
            }
            c = db.query(table, null, where, values, null, null, order, limit)
        }
        return c
    }

    /**
     * 根据多个条件，查询
     * @param table1    表1
     * @param table2    表2
     * @param field1   相等字段
     * @param field2   相等字段
     * @param fields 条件字段
     * @param values 条件值
     * @param order  排序
     * @return
     */
    fun queryAndAll(
        table1: String,
        table2: String,
        field1: String,
        field2: String,
        fields: Array<String>,
        values: Array<String>,
        order: String?
    ): Cursor {
        open()
        var where = ""
        for (i in fields.indices) {
            if (i > 0) where += QUERY_WHERE_AND
            where += " " + fields[i] + " = ? "
        }
        where += " $QUERY_WHERE_AND tb1.$field1 = tb2.$field2"
        return db.query("$table1 as tb1,$table2 as tb2 ", null, where, values, null, null, order)
    }

    /**
     * 根据多个条件，查询
     * @param table1  表1
     * @param table2  表2
     * @param field1  相等字段
     * @param field2  相等字段
     * @param fields 条件字段
     * @param values 条件值
     * @return
     */
    fun queryAndAll(
        table1: String,
        table2: String,
        field1: String,
        field2: String,
        fields: Array<String>,
        values: Array<String>
    ): Cursor {
        return queryAndAll(table1, table2, field1, field2, fields, values, null)
    }

    /**
     * 查询表的某些字段
     * @param table  表名
     * @param columns 字段
     * @return
     */
    fun queryAll(table: String, columns: Array<String>): Cursor {
        open()
        return db.query(table, columns, null, null, null, null, null)
    }

    fun queryAll(table: String, columns: Array<String>, removeRepeat: Boolean): Cursor {
        open()
        return db.query(removeRepeat, table, columns, null, null, null, null, null, null)
    }

    /**
     * 根据条件查询数据
     * @param table   表名
     * @param fields  条件字段
     * @param values  条件值
     * @return
     */
    fun queryAndAll(table: String, fields: Array<String>, values: Array<String>): Cursor? {
        return queryAndAll(table, null, fields, values, null, false)
    }

    /**
     * 根据条件查询数据
     * @param table   表名
     * @param field  条件字段
     * @param value  条件值
     * @return
     */
    fun queryAndAll(table: String, field: String, value: Any): Cursor? {
        return queryAndAll(table, null, arrayOf(field), arrayOf(value.toString()), null, false)
    }

    fun queryAndAll(
        table: String,
        columns: Array<String>,
        field: String,
        value: Any,
        removeRepeat: Boolean
    ): Cursor? {
        return queryAndAll(
            table,
            columns,
            arrayOf(field),
            arrayOf(value.toString()),
            null,
            removeRepeat
        )
    }

    /**
     * 根据条件查询数据
     * @param table   表名
     * @param columns 列
     * @param field  条件字段
     * @param value  条件值
     * @return
     */
    fun queryAndAll(table: String, columns: Array<String>, field: String, value: Any): Cursor? {
        return queryAndAll(table, columns, arrayOf(field), arrayOf(value.toString()), null, false)
    }

    /**
     * 根据条件查询数据
     * @param table   表名
     * @param field  条件字段
     * @param value  条件值
     * @return
     */
    fun queryAndNotAll(
        table: String,
        field: String,
        value: Any,
        notFiled: String,
        notValue: Any,
        order: String
    ): Cursor? {
        return queryAndNotAll(
            table,
            arrayOf(field),
            arrayOf(value.toString()),
            arrayOf(notFiled),
            arrayOf(notValue.toString()),
            order
        )
    }

    /**
     * 根据条件查询数据
     * @param table   表名
     * @param field  条件字段
     * @param value  条件值
     * @return
     */
    fun queryAndAll(table: String, field: String, value: Any, order: String): Cursor? {
        return queryAndAll(table, null, arrayOf(field), arrayOf(value.toString()), order, false)
    }

    /**
     * 根据条件查询数据
     * @param table
     * @param fields
     * @param values
     * @param order
     * @return
     */
    fun queryAndAll(
        table: String,
        fields: Array<String>,
        values: Array<String>,
        order: String
    ): Cursor? {
        return queryAndAll(table, null, fields, values, order, false)
    }

    fun queryAndAll(
        table: String,
        fields: Array<String>,
        values: Array<String>,
        order: String,
        removeRepeat: Boolean
    ): Cursor? {
        return queryAndAll(table, null, fields, values, order, removeRepeat)
    }

    /**
     * 根据条件查询数据
     * @param table   表名
     * @param coloumns 列
     * @param fields  条件字段
     * @param values  条件值
     * @param order 排序方式
     * @return
     */
    fun queryAndAll(
        table: String,
        coloumns: Array<String>?,
        fields: Array<String>?,
        values: Array<String>,
        order: String?,
        removeRepeat: Boolean
    ): Cursor? {
        open()
        var c: Cursor? = null
        if (fields == null || fields.size == 0) {
            c = db.query(removeRepeat, table, null, null, null, null, null, order, null)
        } else {
            var where = ""
            for (i in fields.indices) {
                if (i > 0) where += QUERY_WHERE_AND
                where += " " + fields[i] + " = ? "
            }
            c = db.query(removeRepeat, table, coloumns, where, values, null, null, order, null)
        }
        return c
    }

    /**
     * 根据条件查询数据
     * @param table   表名
     * @param fields  条件字段
     * @param values  条件值
     * @param order 排序方式
     * @return
     */
    fun queryAndNotAll(
        table: String,
        fields: Array<String>?,
        values: Array<String>,
        notFields: Array<String>?,
        notValues: Array<String>,
        order: String
    ): Cursor? {
        open()
        var c: Cursor? = null
        var where = ""
        val len = (fields?.size ?: 0) + (notFields?.size ?: 0)
        val whereValues = if (len != 0) arrayOfNulls<String>(len) else null
        if (fields != null && fields.size > 0) {
            for (i in fields.indices) {
                if (i > 0) where += QUERY_WHERE_AND
                where += " " + fields[i] + " = ? "
            }
            System.arraycopy(values, 0, whereValues!!, 0, fields.size)
        }
        if (notFields != null && notFields.size > 0) {
            if (!TextUtils.isEmpty(where)) where += QUERY_WHERE_AND
            for (i in notFields.indices) {
                if (i > 0) where += QUERY_WHERE_AND
                where += " " + notFields[i] + " != ? "
            }
            System.arraycopy(notValues, 0, whereValues!!, fields?.size ?: 0, notFields.size)
        }
        c = db.query(table, null, where, whereValues, null, null, order)
        return c
    }

    /**
     * 查询大于等于某个字段的数据
     * @param table  表名
     * @param fields 需要查询的字段
     * @param field  指定的字段
     * @param value  值
     * @return
     */
    fun queryGreater(
        table: String,
        fields: Array<String>,
        field: String,
        value: Any,
        orderBy: String
    ): Cursor {
        open()
        return db.query(
            table,
            fields,
            "$field >= ?",
            arrayOf(value.toString()),
            null,
            null,
            orderBy
        )
    }

    /**
     * 查询小于某个字段的数据
     * @param table  表名
     * @param fields 需要查询的字段
     * @param field  指定的字段
     * @param value  值
     * @return
     */
    fun queryLess(
        table: String,
        fields: Array<String>,
        field: String,
        value: Any,
        orderBy: String
    ): Cursor {
        open()
        return db.query(table, fields, "$field < ?", arrayOf(value.toString()), null, null, orderBy)
    }

    fun queryLike(
        table: String,
        fileds: Array<String>,
        values: Array<String>,
        likeField: String,
        like: String,
        orderBy: String
    ): Cursor {
        open()
        var where = ""
        val len = fileds.size
        for (i in 0 until len) {
            if (i != 0) where += QUERY_WHERE_AND
            where += fileds[i] + QUERY_WHERE_PARAM
        }
        where += "$QUERY_WHERE_AND$likeField like '%$like%'"
        return db.query(table, null, where, values, null, null, orderBy)
    }

    /**
     * like查询
     * @param table  表名
     * @param fields 需要查询的字段
     * @param field  指定的字段
     * @param like   值
     * @return
     */
    fun queryLike(table: String, fields: Array<String>, field: String, like: String): Cursor {
        open()
        return db.query(table, fields, "$field like ?", arrayOf("%$like%"), null, null, null)
    }

    /**
     * like查询
     * @param table  表名
     * @param fields 需要查询的字段
     * @param field  指定的字段
     * @param like   值
     * @return
     */
    fun queryLike(
        table: String,
        fields: Array<String>,
        field: String,
        like: String,
        curpage: Int,
        pagesize: Int,
        order: String
    ): Cursor {
        return queryLikeRaw(table, fields, field, like, (curpage - 1) * pagesize, pagesize, order)
    }

    /**
     * like查询
     * @param table
     * @param fields
     * @param field
     * @param like
     * @param beginRow
     * @param pagesize
     * @param order
     * @return
     */
    fun queryLikeRaw(
        table: String,
        fields: Array<String>,
        field: String,
        like: String,
        beginRow: Int,
        pagesize: Int,
        order: String
    ): Cursor {
        open()
        val limit = "$beginRow,$pagesize"
        return db.query(
            table,
            fields,
            "$field like ?",
            arrayOf("%$like%"),
            null,
            null,
            order,
            limit
        )
    }

    /**
     * 查询某个字段属性某一些值的记录
     * @param table   表名
     * @param field   字段名
     * @param values  一些值
     * @param order   排序方式
     * @return
     */
    fun queryInAll(table: String, field: String, values: Array<String>, order: String): Cursor {
        open()
        var value = ""
        for (i in values.indices) {
            if (i != 0) value += ","
            value += "'" + values[i] + "'"
        }
        return db.query(table, null, "$field in($value) ", null, null, null, order)
    }

    /*
	 * 查询某个字段属性某一些值的记录
	 * @param table   表名
	 * @param field   字段名
	 * @param values  一些值
	 * @param order   排序方式
	 * @return
	 */
    private fun queryInAll(
        db: SQLiteDatabase,
        table: String,
        columns: Array<String>,
        field: String,
        values: Array<String>,
        order: String?
    ): Cursor {
        var value = ""
        for (i in values.indices) {
            if (i != 0) value += ","
            value += "'" + values[i] + "'"
        }
        return db.query(table, columns, "$field in($value) ", null, null, null, null)
    }


}