package com.igniteai.app.core.security

import android.content.Context
import com.igniteai.app.core.database.DatabaseProvider
import java.io.File

/**
 * Emergency data destruction.
 *
 * Triggered by triple-tap on app logo → 5-second hold confirm.
 * Deletes all app data, encryption keys, and resets to fresh install.
 *
 * What gets deleted:
 * - Room database (ignite_db.enc)
 * - Audio cache
 * - Vault files
 * - DataStore preferences
 * - Encryption keys from Android Keystore
 *
 * What survives:
 * - Stripe transaction history (held by Stripe, not us)
 * - The APK itself (user must uninstall separately)
 *
 * NOTE: Android file deletion is not forensic-grade.
 * For guaranteed erasure, recommend device factory reset.
 */
class PanicWipeManager(
    private val encryptionManager: EncryptionManager,
) {

    /**
     * Wipe all app data. Returns true if all deletions succeeded.
     *
     * @param context Application context
     * @return True if wipe completed successfully
     */
    fun wipeAllData(context: Context): Boolean {
        var success = true

        // 1. Close and delete the encrypted database
        try {
            DatabaseProvider.reset()
            val dbFile = context.getDatabasePath("ignite_db.enc")
            if (dbFile.exists()) {
                success = dbFile.delete() && success
            }
            // Also delete WAL and SHM files (SQLite journal)
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            File(dbFile.path + "-journal").delete()
        } catch (e: Exception) {
            success = false
        }

        // 2. Delete DataStore preferences
        try {
            val prefsDir = File(context.filesDir, "datastore")
            if (prefsDir.exists()) {
                prefsDir.deleteRecursively()
            }
        } catch (e: Exception) {
            success = false
        }

        // 3. Delete audio cache
        try {
            context.cacheDir.deleteRecursively()
            context.cacheDir.mkdirs() // Recreate empty cache dir
        } catch (e: Exception) {
            success = false
        }

        // 4. Delete all files in internal storage
        try {
            context.filesDir.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
        } catch (e: Exception) {
            success = false
        }

        // 5. Delete encryption keys (makes any surviving data unreadable)
        try {
            encryptionManager.deleteAllKeys()
        } catch (e: Exception) {
            success = false
        }

        return success
    }
}
