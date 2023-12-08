package Connections

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

object UserConfigurationDataSource {

    fun saveUserConfiguration(context: Context, email: String, theme: String, language: String) {
        val dbHelper = AdminSQLIteConection(context)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("email", email)
            put("theme", theme)
            put("language", language)
        }

        // Insertar o actualizar la configuración del usuario
        db.replace("UserConfiguration", null, values)

        db.close()
    }


    @SuppressLint("Range")
    fun getUserConfiguration(context: Context, email: String): Pair<String, String>? {
        val dbHelper = AdminSQLIteConection(context)
        val db = dbHelper.readableDatabase

        val columns = arrayOf("theme", "language")
        val selection = "email = ?"
        val selectionArgs = arrayOf(email)

        // Consultar la configuración del usuario por email
        val cursor: Cursor = db.query("UserConfiguration", columns, selection, selectionArgs, null, null, null)

        val configuration: Pair<String, String>? = if (cursor.moveToFirst()) {
            val theme = cursor.getString(cursor.getColumnIndex("theme"))
            val language = cursor.getString(cursor.getColumnIndex("language"))
            Pair(theme, language)
        } else {
            null
        }

        cursor.close()
        db.close()

        return configuration
    }
}