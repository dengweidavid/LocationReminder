package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

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
    fun loadReminders_shouldReturnError() =
        runTest {
            // GIVEN should return error
            fakeDataSource.setShouldReturnError(true)

            val reminder1 = ReminderDataItem(
                "Title todo1",
                "Description todo1",
                "Location todo1",
                250.0,
                350.0)
            fakeDataSource.saveReminder(reminder1.toReminderDTO())

            // WHEN load reminders
            viewModel.loadReminders()

            // THEN show error message in SnackBar
            MatcherAssert.assertThat(
                viewModel.showSnackBar.value, CoreMatchers.`is`("Reminders not found")
            )
        }

    @Test
    fun check_loading() =
        runTest {
            mainCoroutineRule.pauseDispatcher()

            val reminder1 = ReminderDataItem(
                "Title todo1",
                "Description todo1",
                "Location todo1",
                250.0,
                350.0)
            fakeDataSource.saveReminder(reminder1.toReminderDTO())

            viewModel.loadReminders()

            MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(true))

            mainCoroutineRule.resumeDispatcher()
            MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(false))
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