package crc.DataDefender.DefenderDemo.Room

import CRC.DataProtect.database.SQLiteDatabase
import CRC.DataProtect.database.Room.DataDefenderRoom

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Model::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * @return [ModelDao]
     */
    abstract fun modelDao(): ModelDao

    companion object {
        const val DB_NAME = "app.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val databaseFilePath = context.getDatabasePath(DB_NAME).absolutePath
            SQLiteDatabase.openOrCreateDatabase(databaseFilePath, null).close()

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = DataDefenderRoom.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build()

                INSTANCE = instance
                return instance
            }
        }

    }
}