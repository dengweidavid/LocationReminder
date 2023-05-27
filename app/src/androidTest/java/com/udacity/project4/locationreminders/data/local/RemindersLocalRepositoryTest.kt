package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.wrapEspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var localRepository: RemindersLocalRepository

    @Before
    fun setupLocalRepository() {
        wrapEspressoIdlingResource{
            database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
            ).allowMainThreadQueries().build()

            localRepository =
                RemindersLocalRepository(
                    database.reminderDao(), Dispatchers.Main
                )
        }
    }

    @After
    fun cleanUp() = database.close()

    @Test
    fun reminderRepo_getReminder_verifyCorrectData() = runBlocking{
        wrapEspressoIdlingResource{
            // GIVEN - Insert a reminder.
            val data = ReminderDTO(
                "title todo",
                "description todo",
                "location todo",
                100.00,
                250.00
            )
            localRepository.saveReminder(data)

            // WHEN - Get the reminder by id from the local repo.
            val result = localRepository.getReminder(data.id)

            // THEN Result is not null
            // Success is return
            val success = (result is Result.Success)
            Assert.assertEquals(true, success)
            // The loaded data contains the expected values.
            result as Result.Success
            Assert.assertEquals(data.title, result.data.title)
            Assert.assertEquals(data.description, result.data.description)
            Assert.assertEquals(data.location, result.data.location)
            Assert.assertEquals(data.latitude, result.data.latitude)
            Assert.assertEquals(data.longitude, result.data.longitude)
        }
    }

    @Test
    fun reminderRepo_getReminder_returnError() = runBlocking {
        wrapEspressoIdlingResource{
            // GIVEN - Database is empty
            localRepository.deleteAllReminders() //make sure the repository is empty

            // WHEN - Get the reminder by id from the database.
            val result = localRepository.getReminder("123")

            // THEN Error is return.
            val error = (result is Result.Error)
            Assert.assertEquals(true, error)
        }
    }

    @Test
    fun reminderRepo_deleteAllReminders_returnZeroSize() = runBlocking {
        wrapEspressoIdlingResource{
            // GIVEN - Database is empty
            localRepository.deleteAllReminders() //make sure the repository is empty

            // WHEN - Get the reminder by id from the database.
            val result = localRepository.getReminders() as Result.Success

            // THEN size is 0.
            Assert.assertEquals(0, result.data.size)
        }
    }

}