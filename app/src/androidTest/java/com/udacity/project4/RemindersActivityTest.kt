package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addReminderActivity_validFlow_success() =
        runBlocking {
            val typingTitle = "Title espresso"
            val typingDescription = "Description espresso"

            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            var activity: Activity? = null
            activityScenario.onActivity {
                activity = it
            }

            Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
                .perform(ViewActions.replaceText(typingTitle))
            Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
                .perform(ViewActions.replaceText(typingDescription))
            Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withId(R.id.mapFragment))
                .perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withId(R.id.buttonSaveLocation))
                .perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

            // Verify toast is shown correctly!
            Espresso.onView(withText(R.string.reminder_saved))
                .inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.`is`(activity!!.window.decorView))))
                .check(
                    ViewAssertions.matches(
                        ViewMatchers.isDisplayed()
                    )
                )

            // Verify: One item is created
            Espresso.onView(withText(typingTitle))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(withText(typingDescription))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


            // Click on that item
            Espresso.onView(withText(typingTitle)).perform(ViewActions.click())

            // Verify detail screen is correct!
            Espresso.onView(withText(typingTitle))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(withText(typingDescription))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            activityScenario.close()
        }

    @Test
    fun addReminderActivity_validFlow_failure() =
        runBlocking {
            val typingTitle = "Title espresso"
            val typingDescription = "Description espresso"

            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
                .perform(ViewActions.replaceText(typingTitle))
            Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
                .perform(ViewActions.replaceText(typingDescription))
            Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

            // Verify that location is not selected
            Espresso.onView(
                CoreMatchers.allOf(
                    ViewMatchers.withId(com.google.android.material.R.id.snackbar_text),
                    withText(R.string.err_select_location)
                )
            ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            activityScenario.close()
        }
}
