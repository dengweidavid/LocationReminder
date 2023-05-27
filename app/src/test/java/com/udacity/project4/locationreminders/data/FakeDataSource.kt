package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    // Create a fake data source to act as a double to the real data source
    var reminders = mutableListOf<ReminderDTO>()

    private var shouldReturnError = false

    fun setShouldReturnError(error: Boolean) {
        shouldReturnError = error
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Reminders not found!")
        } else {
            reminders.let { return Result.Success(it) }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders.firstOrNull { it.id == id }?.let { return Result.Success(it) }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}