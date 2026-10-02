package com.gymhelper.app

import android.app.Application
import com.gymhelper.app.data.AppDatabase
import com.gymhelper.app.data.GymRepository

class GymHelperApp : Application() {
    lateinit var repository: GymRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = GymRepository(db.intervalDao(), db.weightDao())
    }
}
