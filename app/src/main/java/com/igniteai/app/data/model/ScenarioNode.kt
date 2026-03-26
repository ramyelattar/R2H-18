package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single node in a branching roleplay scenario tree.
 *
 * Scenarios are choose-your-own-adventure style narratives.
 * Each node has text (the story beat) and 2-3 choices that
 * lead to different child nodes.
 *
 * Structure: root node → choice → child node → choice → ...
 * Leaf nodes (no choices) end the scenario.
 */
@Entity(
    tableName = "scenario_node",
    indices = [Index("scenarioId"), Index("parentNodeId")],
)
data class ScenarioNode(
    @PrimaryKey val id: String,
    val scenarioId: String,         // Groups nodes into one scenario
    val parentNodeId: String?,      // Null for root node
    val choiceIndex: Int = 0,       // Which choice from parent led here (0,1,2)
    val text: String,               // Narrative text displayed to couple
    val audioRef: String? = null,   // Optional audio to play at this node
    val hapticPattern: String? = null, // Optional haptic pattern name
    val choicesJson: String = "[]", // JSON array of choice labels: ["Do X", "Do Y"]
    val isRoot: Boolean = false,
    val tone: String = "PLAYFUL",
    val intensity: Int = 1,
)
