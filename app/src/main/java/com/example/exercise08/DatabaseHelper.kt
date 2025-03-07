package com.example.exercise08

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var readableDB: SQLiteDatabase? = null
    private var writableDB: SQLiteDatabase? = null

    companion object {
        private const val DATABASE_NAME = "courses.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_COURSES = "courses"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DESCRIPTION = "description"
    }

    // Phương thức để lấy readable database
    private fun getReadDB(): SQLiteDatabase {
        if (readableDB == null || !readableDB!!.isOpen) {
            readableDB = this.readableDatabase
        }
        return readableDB!!
    }

    // Phương thức để lấy writable database
    private fun getWriteDB(): SQLiteDatabase {
        if (writableDB == null || !writableDB!!.isOpen) {
            writableDB = this.writableDatabase
        }
        return writableDB!!
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Table
        val createTableStatement = ("CREATE TABLE $TABLE_COURSES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_DESCRIPTION TEXT)")
        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(
        db: SQLiteDatabase, oldVersion: Int,
        newVersion: Int
    ) {
        // Drop Table
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        onCreate(db)
    }

    fun addCourse(course: Course): Boolean {
        val db = getWriteDB()
        // Get values
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, course.name)
            put(COLUMN_DESCRIPTION, course.description)
        }
        // Insert
        val result = db.insert(
            TABLE_COURSES, null,
            contentValues
        )
        return result != -1L
    }

    @SuppressLint("Range")
    fun getCourses(): List<Course> {
        val courseList = mutableListOf<Course>()
        val db = getReadDB()
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_COURSES", null)
        // Add to List
        if (cursor.moveToFirst()) {
            do {
                val id =
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID))

                val name =
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val description =
                    cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION))
                courseList.add(Course(id, name, description))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courseList
    }

    fun updateCourse(course: Course): Boolean {
        val db = getWriteDB()
        // Get values
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, course.name)
            put(COLUMN_DESCRIPTION, course.description)
        }
        // Update course
        val result = db.update(
            TABLE_COURSES, contentValues,
            "$COLUMN_ID = ?", arrayOf(course.id.toString())
        )
        return result > 0 // Returns true if result > 0
    }

    fun deleteCourse(id: Int): Boolean {
        val db = getWriteDB()
        // Delete course
        val result = db.delete(
            TABLE_COURSES, "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        return result > 0 // Returns true if result > 0
    }

    fun closeDatabase() {
        readableDB?.close()
        writableDB?.close()
        readableDB = null
        writableDB = null
    }
}
