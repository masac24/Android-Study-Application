package com.example.diplomski.retrofit

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    fun saveDailyGoal(goal: Int) = prefs.edit().putInt("daily_goal", goal).apply()
    fun getDailyGoal(): Int = prefs.getInt("daily_goal", 1)

    fun saveNumberOfQuestions(num: Int) = prefs.edit().putInt("num_questions", num).apply()
    fun getNumberOfQuestions(): Int = prefs.getInt("num_questions", 10)
}