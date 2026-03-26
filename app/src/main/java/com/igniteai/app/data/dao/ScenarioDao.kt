package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igniteai.app.data.model.ScenarioNode

@Dao
interface ScenarioDao {

    @Query("SELECT * FROM scenario_node WHERE scenarioId = :scenarioId AND isRoot = 1")
    suspend fun getRootNode(scenarioId: String): ScenarioNode?

    @Query("SELECT * FROM scenario_node WHERE id = :nodeId")
    suspend fun getNode(nodeId: String): ScenarioNode?

    @Query("SELECT * FROM scenario_node WHERE parentNodeId = :parentId ORDER BY choiceIndex")
    suspend fun getChildren(parentId: String): List<ScenarioNode>

    @Query("SELECT DISTINCT scenarioId FROM scenario_node WHERE isRoot = 1")
    suspend fun getAllScenarioIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<ScenarioNode>)

    @Query("DELETE FROM scenario_node")
    suspend fun deleteAll()
}
