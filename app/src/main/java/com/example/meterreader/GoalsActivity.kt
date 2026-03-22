package com.example.meterreader

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class GoalsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddGoal: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        dbHelper = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerViewGoals)
        btnAddGoal = findViewById(R.id.btnAddGoal)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddGoal.setOnClickListener {
            startActivity(Intent(this, AddGoalActivity::class.java))
        }

        loadGoals()
    }

    override fun onResume() {
        super.onResume()
        loadGoals()
    }

    private fun loadGoals() {
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = dateFormat.format(Date())
        val goals = dbHelper.getGoalsForMonth(currentMonth)
        val adapter = GoalAdapter(goals, dbHelper) {
            loadGoals()
        }
        recyclerView.adapter = adapter
    }
}
