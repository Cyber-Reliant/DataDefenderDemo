package crc.DataDefender.DefenderDemo.Room

import android.content.Context
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class RoomTest {

    companion object {

        private val TAG = RoomTest::class.java.simpleName
        fun test_single_insert(context: Context) {
            Log.d(TAG, "testing single model insert")
            val appdb = AppDatabase.getInstance(context)
            GlobalScope.launch {
                appdb.modelDao().insert(Model(var1 = ""))
            }
        }
        fun test_multi_insert(context: Context) {
            Log.d(TAG, "testing multi model insert")
            val appdb = AppDatabase.getInstance(context)
            GlobalScope.launch {
                appdb.modelDao().insert(
                    Model(var1 = ""),
                    Model(var1 = ""),
                    Model(var1 = "")
                )
            }
        }

        fun test_get_all(context: Context) {
            Log.d(TAG, "testing get all")
            val appdb = AppDatabase.getInstance(context)
            GlobalScope.launch {
                val models = appdb.modelDao().getAllModels().toList()
            }
        }
        fun test_get_by_id(context: Context) {
            Log.d(TAG, "testing get by id")
            val appdb = AppDatabase.getInstance(context)
            GlobalScope.launch {
                appdb.modelDao().getModelById(1)
            }
        }

        fun test_delete_all(context: Context) {

            Log.d(TAG, "testing delete all")
            val appdb = AppDatabase.getInstance(context)
            GlobalScope.launch {
                appdb.modelDao().deleteAllModels()
            }
        }
    }
}