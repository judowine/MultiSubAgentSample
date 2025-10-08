package com.example.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("app_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}
