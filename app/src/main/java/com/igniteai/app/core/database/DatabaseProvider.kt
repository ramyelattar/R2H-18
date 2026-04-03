package com.igniteai.app.core.database

import android.content.Context
import androidx.room.Room
import net.sqlcipher.database.SupportFactory

/**
 * Creates and provides the encrypted Room database instance.
 *
 * Uses SQLCipher's SupportFactory to transparently encrypt all
 * database reads/writes with AES-256. The encryption key comes
 * from EncryptionManager (Task 5) which derives it from Android Keystore.
 *
 * Usage:
 *   val passphrase = encryptionManager.generateDatabaseKey()
 *   val db = DatabaseProvider.create(context, passphrase)
 */
object DatabaseProvider {

    @Volatile
    private var instance: R2H18Database? = null

    /**
     * Creates or returns the singleton encrypted database.
     *
     * @param context Application context
     * @param passphrase 32-byte AES key from EncryptionManager
     * @return Encrypted R2H18Database instance
     */
    fun create(context: Context, passphrase: ByteArray): R2H18Database {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context, passphrase).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context, passphrase: ByteArray): R2H18Database {
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context.applicationContext,
            R2H18Database::class.java,
            "ignite_db.enc"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration() // V1.0: OK to wipe on schema change
            .build()
    }

    /**
     * Clears the singleton for testing or panic wipe.
     */
    fun reset() {
        instance?.close()
        instance = null
    }
}
