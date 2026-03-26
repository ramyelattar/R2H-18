package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single piece of content: dare, text template, audio script, or scenario.
 *
 * Content is loaded from JSON on first launch and stored in Room.
 * The adaptive algorithm queries this table to select personalized content.
 */
@Entity(tableName = "content_item")
data class ContentItem(
    @PrimaryKey val id: String,
    val type: String,               // "DARE", "TEXT", "AUDIO", "SCENARIO"
    val tone: String,               // "PLAYFUL", "RAW", "SENSUAL"
    val intensity: Int,             // 1-10
    val level: String,              // "SPARK" (free) or "FIRE" (paid)
    val duration: String,           // "QUICK", "MEDIUM", "EXTENDED"
    val title: String,
    val body: String,               // Main content text
    val audioRef: String? = null,   // Reference to audio file (res/raw or TTS script)
    val tags: String = "",          // Comma-separated tags for fantasy matching
)
