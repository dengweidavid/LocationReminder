package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var app: Application

    private val reminder = ReminderDataItem(
        "Title todo1",
        "Description todo1",
        "Location todo1",
        250.0,
        350.0)

    @Before
    fun setupSaveReminderViewModel() {
        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )

        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun saveReminder_validReminder_Success() =
        runTest {
            // GIVEN item
            val item = reminder.copy()

            // WHEN save reminder
            viewModel.saveReminder(item)

            // THEN
            // toast message is correct
            Assert.assertEquals(app.getString(R.string.reminder_saved), viewModel.showToast.getOrAwaitValue())
            // navigation is correct
            Assert.assertEquals(NavigationCommand.Back, viewModel.navigationCommand.getOrAwaitValue())
        }

    @Test
    fun validateEnteredData_emptyTitle_Failure() =
        runTest {
            // GIVEN reminder with empty title
            val item = reminder.copy()
            item.title = ""

            // WHEN
            val res = viewModel.validateEnteredData(item)

            // THEN
            Assert.assertEquals(R.string.err_enter_title, viewModel.showSnackBarInt.getOrAwaitValue())
            Assert.assertEquals(false, res)
        }

    @Test
    fun validateEnteredData_nullTitle_Failure() =
        runTest {
            // GIVEN reminder with empty title
            val item = reminder.copy()
            item.title = null

            // WHEN
            val res = viewModel.validateEnteredData(item)

            // THEN
            Assert.assertEquals(R.string.err_enter_title, viewModel.showSnackBarInt.getOrAwaitValue())
            Assert.assertEquals(false, res)
        }

    @Test
    fun validateEnteredData_nullLocation_Failure() =
        runTest {
            // GIVEN reminder with null location
            val item = reminder.copy()
            item.location = null

            // WHEN
            val res = viewModel.validateEnteredData(item)

            // THEN
            Assert.assertEquals(R.string.err_select_location, viewModel.showSnackBarInt.getOrAwaitValue())
            Assert.assertEquals(false, res)
        }

    @Test
    fun validateEnteredData_emptyLocation_Failure() =
        runTest {
            // GIVEN reminder with empty location
            val item = reminder.copy()
            item.location = ""

            // WHEN
            val res = viewModel.validateEnteredData(item)

            // THEN
            Assert.assertEquals(R.string.err_select_location, viewModel.showSnackBarInt.getOrAwaitValue())
            Assert.assertEquals(false, res)
        }

    @Test
    fun validateEnteredData_validReminder_Success()  =
        runTest {
            // GIVEN valid data
            val item = reminder.copy()

            // WHEN
            val res = viewModel.validateEnteredData(item)

            // THEN
            Assert.assertEquals(true, res)
        }
    }

