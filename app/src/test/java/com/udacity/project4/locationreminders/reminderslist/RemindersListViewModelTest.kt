package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupReminderListViewModel() {
        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_givenErrorDataSource_showsSnackBarErrorMessageAndShowNoData() =
        runTest {
            // GIVEN: the dataSource return errors.
            fakeDataSource.setShouldReturnError(true)
            val errorMessage = "Reminders not found!"

            // WHEN load reminders
            viewModel.loadReminders()

            // THEN
            // Show error message in SnackBar
            Assert.assertEquals(errorMessage, viewModel.showSnackBar.getOrAwaitValue())
            // showNoData is true
            Assert.assertEquals(true, viewModel.showNoData.getOrAwaitValue())
        }

    @Test
    fun loadReminders_givenValidDataSource_showsReminderList() =
        runTest {
            // GIVEN items
            val reminder1 = ReminderDataItem(
                "Title todo1",
                "Description todo1",
                "Location todo1",
                250.0,
                350.0)
            fakeDataSource.saveReminder(reminder1.toReminderDTO())

            val reminder2 = ReminderDataItem(
                "Title todo2",
                "Description todo2",
                "Location todo2",
                -250.0,
                350.0)
            fakeDataSource.saveReminder(reminder2.toReminderDTO())

            // WHEN load reminders
            viewModel.loadReminders()

            // THEN
            // data is the same with source
            Assert.assertEquals(listOf(reminder1,reminder2),viewModel.remindersList.getOrAwaitValue())
            // showNoData is false
            Assert.assertEquals(false, viewModel.showNoData.getOrAwaitValue())
        }

    @Test
    fun loadReminders_emptyDataSource_noReminders() =
        runTest {
            // GIVEN no items
            fakeDataSource.deleteAllReminders()

            // WHEN load reminders
            viewModel.loadReminders()

            // THEN
            // Size is zero
            val loadedItems = viewModel.remindersList.getOrAwaitValue()
            Assert.assertEquals(0, loadedItems.size)
            // showNoData is true
            Assert.assertEquals(true, viewModel.showNoData.getOrAwaitValue())
        }
}