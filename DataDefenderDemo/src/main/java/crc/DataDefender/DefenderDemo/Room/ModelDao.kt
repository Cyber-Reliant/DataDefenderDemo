package crc.DataDefender.DefenderDemo.Room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [crc.DataDefender.DefenderDemo.Room.Model]
 */
@Dao
interface ModelDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: Model): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg models: Model)

    /**
     * Inserts [models] into the [Model.TABLE_NAME] table.
     * Duplicate values are replaced in the table.
     * @param models models
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertModels(models: List<Model>)

    /**
     * Deletes all the models from the [Model.TABLE_NAME] table.
     */
    @Query("DELETE FROM ${Model.TABLE_NAME}")
    fun deleteAllModels()

    /**
     * Fetches the model from the [Model.TABLE_NAME] table whose id is [modelId].
     * @param modelId Unique ID of [Model]
     * @return [Flow] of [Model] from database table.
     */
    @Query("SELECT * FROM ${Model.TABLE_NAME} WHERE ID = :modelId")
    fun getModelById(modelId: Int): Flow<Model>

    /**
     * Fetches all the models from the [Model.TABLE_NAME] table.
     * @return [Flow]
     */
    @Query("SELECT * FROM ${Model.TABLE_NAME}")
    fun getAllModels(): Flow<List<Model>>

    /**
     * Get all model
     */
    @Query("SELECT * FROM ${Model.TABLE_NAME}")
    fun getAllModelsList(): List<Model>
}