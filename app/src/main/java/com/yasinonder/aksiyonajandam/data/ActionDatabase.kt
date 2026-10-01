package com.yasinonder.aksiyonajandam.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ActionDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "aksiyon_ajandam.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE actions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                subject TEXT NOT NULL,
                notes TEXT NOT NULL,
                type TEXT NOT NULL,
                priority TEXT NOT NULL,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                alarm_enabled INTEGER NOT NULL,
                completed INTEGER NOT NULL,
                image_uris TEXT NOT NULL,
                extra1 TEXT NOT NULL,
                extra2 TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    @Synchronized
    fun insert(item: ActionItem): Long {
        val values = ContentValues().apply {
            put("title", item.title)
            put("subject", item.subject)
            put("notes", item.notes)
            put("type", item.type)
            put("priority", item.priority)
            put("date", item.date)
            put("time", item.time)
            put("alarm_enabled", if (item.alarmEnabled) 1 else 0)
            put("completed", if (item.completed) 1 else 0)
            put("image_uris", item.imageUris.joinToString(IMAGE_SEPARATOR))
            put("extra1", item.extra1)
            put("extra2", item.extra2)
            put("created_at", item.createdAt)
        }
        return writableDatabase.insertOrThrow("actions", null, values)
    }

    @Synchronized
    fun all(): List<ActionItem> {
        readableDatabase.query(
            "actions",
            null,
            null,
            null,
            null,
            null,
            "date ASC, time ASC, id DESC"
        ).use { cursor ->
            return buildList {
                while (cursor.moveToNext()) add(cursor.toAction())
            }
        }
    }

    @Synchronized
    fun byId(id: Long): ActionItem? {
        readableDatabase.query(
            "actions",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toAction() else null
        }
    }

    @Synchronized
    fun setCompleted(id: Long, completed: Boolean) {
        val values = ContentValues().apply {
            put("completed", if (completed) 1 else 0)
        }
        writableDatabase.update("actions", values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun pendingAlarmItems(): List<ActionItem> {
        readableDatabase.query(
            "actions",
            null,
            "alarm_enabled = 1 AND completed = 0",
            null,
            null,
            null,
            "date ASC, time ASC"
        ).use { cursor ->
            return buildList {
                while (cursor.moveToNext()) add(cursor.toAction())
            }
        }
    }

    private fun Cursor.toAction(): ActionItem {
        fun str(name: String) = getString(getColumnIndexOrThrow(name))
        fun long(name: String) = getLong(getColumnIndexOrThrow(name))
        fun bool(name: String) = getInt(getColumnIndexOrThrow(name)) == 1

        val images = str("image_uris")
            .split(IMAGE_SEPARATOR)
            .filter { it.isNotBlank() }

        return ActionItem(
            id = long("id"),
            title = str("title"),
            subject = str("subject"),
            notes = str("notes"),
            type = str("type"),
            priority = str("priority"),
            date = str("date"),
            time = str("time"),
            alarmEnabled = bool("alarm_enabled"),
            completed = bool("completed"),
            imageUris = images,
            extra1 = str("extra1"),
            extra2 = str("extra2"),
            createdAt = long("created_at")
        )
    }

    companion object {
        private const val IMAGE_SEPARATOR = "\u001F"

        @Volatile
        private var instance: ActionDatabase? = null

        fun get(context: Context): ActionDatabase =
            instance ?: synchronized(this) {
                instance ?: ActionDatabase(context).also { instance = it }
            }
    }
}
