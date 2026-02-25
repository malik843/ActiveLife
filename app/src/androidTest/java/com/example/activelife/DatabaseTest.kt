package com.example.activelife

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.activelife.data.ActivityDao
import com.example.activelife.data.ActivityLog
import com.example.activelife.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ActivityDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Create an in-memory DB so it disappears after testing
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.activityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() = runBlocking {
        // 1. Create a dummy log
        val log = ActivityLog(activityType = "WALKING", timestamp = 123456789L)

        // 2. Insert it
        dao.insertLog(log)

        // 3. Read it back
        val recentLogs = dao.getLogsSince(123456780L)

        // 4. VERIFY: Did we get 1 item? Is it "WALKING"?
        assertEquals(1, recentLogs.size)
        assertEquals("WALKING", recentLogs[0].activityType)
    }
}