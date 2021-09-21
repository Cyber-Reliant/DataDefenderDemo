package crc.DataDefender.DefenderDemo.Room

import androidx.room.Entity
import androidx.room.PrimaryKey
import crc.DataDefender.DefenderDemo.Room.Model.Companion.TABLE_NAME

/**
 * Data class for Database entity and Serialization.
 */
@Entity(tableName = TABLE_NAME)
data class Model(

    @PrimaryKey(autoGenerate = true) var id: Int? = 0,
    var var1: String? = null
) {
    companion object {
        const val TABLE_NAME = "models"
    }
}