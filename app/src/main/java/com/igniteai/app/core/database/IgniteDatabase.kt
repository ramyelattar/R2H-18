package com.igniteai.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.igniteai.app.data.dao.ContentDao
import com.igniteai.app.data.dao.EngagementDao
import com.igniteai.app.data.dao.FantasyDao
import com.igniteai.app.data.dao.PairingDao
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.data.model.CoupleProfile
import com.igniteai.app.data.model.EngagementRecord
import com.igniteai.app.data.model.FantasyProfile
import com.igniteai.app.data.model.LicenseKey
import com.igniteai.app.data.model.PairingData
import com.igniteai.app.data.model.Partner
import com.igniteai.app.data.model.ScenarioNode
import com.igniteai.app.data.model.SessionRecord
import com.igniteai.app.data.model.VaultItem

/**
 * IgniteAI Room Database.
 *
 * Encrypted with SQLCipher (AES-256). All couple data, content,
 * sessions, and preferences live here — never on a server.
 *
 * The database is opened via DatabaseProvider which supplies
 * the SQLCipher encryption key derived from Android Keystore.
 */
@Database(
    entities = [
        CoupleProfile::class,
        Partner::class,
        PairingData::class,
        ContentItem::class,
        SessionRecord::class,
        EngagementRecord::class,
        FantasyProfile::class,
        VaultItem::class,
        ScenarioNode::class,
        LicenseKey::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class IgniteDatabase : RoomDatabase() {

    abstract fun contentDao(): ContentDao
    abstract fun engagementDao(): EngagementDao
    abstract fun pairingDao(): PairingDao
    abstract fun fantasyDao(): FantasyDao

    // DAOs added as each feature module is built:
    // abstract fun sessionDao(): SessionDao
    // abstract fun vaultDao(): VaultDao
    // abstract fun scenarioDao(): ScenarioDao
    // abstract fun licenseDao(): LicenseDao
}
