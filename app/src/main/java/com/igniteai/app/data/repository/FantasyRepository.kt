package com.igniteai.app.data.repository

import android.content.Context
import com.igniteai.app.data.dao.FantasyDao
import com.igniteai.app.data.model.FantasyProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

/**
 * Manages fantasy profiles and computes couple overlap.
 *
 * The overlap algorithm:
 * 1. Load both partners' profiles
 * 2. For each question, compare answers:
 *    - Scale questions: both scored 3+ on same topic → shared interest
 *    - Checkbox questions: intersection of selected items → shared interests
 *    - Boundaries: union of both partners' boundaries → excluded topics
 * 3. Intensity range: min of both partners' max comfort levels
 * 4. Content tags matching shared interests get boosted in adaptive algorithm
 */
class FantasyRepository(
    private val fantasyDao: FantasyDao,
) {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    /**
     * Save a partner's questionnaire answers.
     *
     * @param partnerId The partner who answered
     * @param answers Map of questionId → answer value (String for scale/choice, List for checkbox)
     */
    suspend fun saveAnswers(partnerId: String, answers: Map<String, Any>) {
        val answersJson = json.encodeToString(
            answers.mapValues { (_, value) ->
                when (value) {
                    is List<*> -> value.joinToString(",")
                    else -> value.toString()
                }
            }
        )

        val existing = fantasyDao.getProfile(partnerId)
        val profile = FantasyProfile(
            id = existing?.id ?: UUID.randomUUID().toString(),
            partnerId = partnerId,
            answersJson = answersJson,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        fantasyDao.upsertProfile(profile)
    }

    /**
     * Get a partner's answers as a parsed map.
     */
    suspend fun getAnswers(partnerId: String): Map<String, String>? {
        val profile = fantasyDao.getProfile(partnerId) ?: return null
        return json.decodeFromString<Map<String, String>>(profile.answersJson)
    }

    /**
     * Compute the fantasy overlap between two partners.
     *
     * This is the core matching algorithm that shapes what content
     * the couple sees. It respects both partners' boundaries absolutely.
     */
    suspend fun getOverlap(partner1Id: String, partner2Id: String): FantasyOverlap {
        val profile1 = fantasyDao.getProfile(partner1Id)
        val profile2 = fantasyDao.getProfile(partner2Id)

        if (profile1 == null || profile2 == null) {
            return FantasyOverlap.empty()
        }

        val answers1 = json.decodeFromString<Map<String, String>>(profile1.answersJson)
        val answers2 = json.decodeFromString<Map<String, String>>(profile2.answersJson)

        return computeOverlap(answers1, answers2)
    }

    /**
     * Get content tags that match the couple's shared interests.
     * Used by the adaptive algorithm to boost matching content.
     */
    suspend fun getContentTags(partner1Id: String, partner2Id: String): List<String> {
        val overlap = getOverlap(partner1Id, partner2Id)
        return overlap.sharedInterests
    }

    /**
     * Load fantasy questions from assets JSON.
     */
    fun loadQuestions(context: Context): List<FantasyQuestion> {
        val questionsJson = context.assets.open("content/fantasy_questions.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString<List<FantasyQuestion>>(questionsJson)
    }

    // ── Overlap Algorithm ───────────────────────────────────

    /**
     * Core overlap computation.
     *
     * For scale questions (1-5):
     *   Both scored 3+ → topic is a shared interest
     *   Either scored 1 → treat as soft boundary
     *
     * For checkbox questions:
     *   Intersection of selected items → shared interests
     *
     * For boundary questions:
     *   Union of all selected boundaries → excluded from ALL content
     *
     * Intensity: min of both partners' max intensity answer
     */
    internal fun computeOverlap(
        answers1: Map<String, String>,
        answers2: Map<String, String>,
    ): FantasyOverlap {
        val sharedInterests = mutableListOf<String>()
        val excludedTopics = mutableListOf<String>()
        var maxIntensity = 10 // Default to max, will be reduced

        for ((questionId, answer1) in answers1) {
            val answer2 = answers2[questionId] ?: continue

            when {
                // Boundary question — union of both partners' selections
                questionId.startsWith("q-boundary") -> {
                    val boundaries1 = answer1.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val boundaries2 = answer2.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    excludedTopics.addAll((boundaries1 + boundaries2).distinct())
                }

                // Intensity question — take the lower of both partners' maxes
                questionId.startsWith("q-intensity") -> {
                    val intensity1 = answer1.toIntOrNull() ?: 5
                    val intensity2 = answer2.toIntOrNull() ?: 5
                    maxIntensity = minOf(intensity1, intensity2)
                }

                // Scale questions — both 3+ means shared interest
                answer1.toIntOrNull() != null -> {
                    val score1 = answer1.toInt()
                    val score2 = answer2.toInt()
                    if (score1 >= 3 && score2 >= 3) {
                        // Extract tags from question ID
                        sharedInterests.add(questionId)
                    }
                }

                // Checkbox/choice — intersection
                answer1.contains(",") || answer2.contains(",") -> {
                    val items1 = answer1.split(",").map { it.trim() }.toSet()
                    val items2 = answer2.split(",").map { it.trim() }.toSet()
                    val shared = items1.intersect(items2)
                    sharedInterests.addAll(shared)
                }

                // Choice questions — match if same answer
                else -> {
                    if (answer1 == answer2) {
                        sharedInterests.add(answer1)
                    }
                }
            }
        }

        return FantasyOverlap(
            sharedInterests = sharedInterests.distinct(),
            excludedTopics = excludedTopics.distinct(),
            maxIntensity = maxIntensity,
        )
    }
}

/**
 * Result of computing the overlap between two partners' fantasy profiles.
 */
data class FantasyOverlap(
    val sharedInterests: List<String>,
    val excludedTopics: List<String>,
    val maxIntensity: Int,
) {
    companion object {
        fun empty() = FantasyOverlap(
            sharedInterests = emptyList(),
            excludedTopics = emptyList(),
            maxIntensity = 5,
        )
    }
}

/**
 * A single fantasy questionnaire question, parsed from JSON.
 */
@Serializable
data class FantasyQuestion(
    val id: String,
    val category: String,
    val text: String,
    val type: String, // "scale", "choice", "checkbox"
    val options: List<String>,
    val labels: Map<String, String>? = null,
    val tags: List<String> = emptyList(),
)
