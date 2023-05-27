package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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

    @Test
    fun loadReminders_givenErrorResult_showsSnackBarErrorMessage() =
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
}