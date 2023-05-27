package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO(
        "Title todo 1",
        "Description todo 1",
        "Location todo 1",
        100.0,
        250.0)

    private val reminder2 = ReminderDTO(
        "Title todo 2",
        "Description todo 2",
        "Location todo 2",
        250.0,
        100.0)

    @Before
    fun initDatabase() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun reminderDao_getReminderById_verifyCorrectData() = runBlocking {
        // GIVEN - Insert a reminder.
        database.reminderDao().saveReminder(reminder1)

        // WHEN - Get the reminder by id from the database.
        val result: ReminderDTO? = database.reminderDao().getReminderById(reminder1.id)

        // THEN - The loaded data contains the expected values.
        Assert.assertEquals(reminder1, result)
    }

    @Test
    fun reminderDao_getReminderById_returnNull() = runBlocking {
        // GIVEN - Database is empty
        database.reminderDao().deleteAllReminders()

        // WHEN - Get the reminder by id from the database.
        val result = database.reminderDao().getReminderById(reminder1.id)

        // THEN
        // Null is return
        Assert.assertEquals(null, result)
        // Size is 0
        val loadedReminders = database.reminderDao().getReminders()
        Assert.assertEquals(0, loadedReminders.size)
    }

    @Test
    fun reminderDao_getReminders_verifyCorrectData() = runBlocking {
        // GIVEN - Insert two reminders.
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Load all reminders from the database.
        val loadedReminders = database.reminderDao().getReminders()

        // THEN
        // Data is correct
        Assert.assertEquals(reminder1, loadedReminders[0])
        Assert.assertEquals(reminder2, loadedReminders[1])
        // Size is 2
        Assert.assertEquals(2, loadedReminders.size)
    }

}