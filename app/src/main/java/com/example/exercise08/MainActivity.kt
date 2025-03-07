package com.example.exercise08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        databaseHelper = DatabaseHelper(this)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SQLiteCourseScreen(databaseHelper)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.closeDatabase()
    }
}

@Composable
fun SQLiteCourseScreen(databaseHelper: DatabaseHelper) {
    var courseName by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }

    var isUpdating by remember { mutableStateOf(false) }
    var currentCourseId by remember { mutableIntStateOf(0) }

    fun refreshCourses() {
        courses = databaseHelper.getCourses()
    }

    LaunchedEffect(Unit) {
        refreshCourses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "SQLite_Course",
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
                .padding(16.dp),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Input fields
        OutlinedTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = { Text("Course Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = courseDescription,
            onValueChange = { courseDescription = it },
            label = { Text("Course Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        // Add/Update button
        Button(
            onClick = {
                if (courseName.isNotEmpty()) {
                    if (isUpdating) {
                        val updatedCourse = Course(
                            id = currentCourseId,
                            name = courseName,
                            description = courseDescription
                        )
                        val success = databaseHelper.updateCourse(updatedCourse)
                        if (success) {
                            isUpdating = false
                            currentCourseId = 0
                        }
                    } else {
                        val newCourse = Course(
                            id = 0,
                            name = courseName,
                            description = courseDescription
                        )
                        databaseHelper.addCourse(newCourse)
                    }

                    refreshCourses()
                    courseName = ""
                    courseDescription = ""
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text(if (isUpdating) "UPDATE COURSE" else "ADD COURSE")
        }

        if (isUpdating) {
            Button(
                onClick = {
                    isUpdating = false
                    currentCourseId = 0
                    courseName = ""
                    courseDescription = ""
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("CANCEL")
            }
        }

        // Database label
        Text(
            text = "db",
            modifier = Modifier.padding(top = 16.dp),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "database",
            color = Color.Gray,
            fontSize = 12.sp
        )

        // Course list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            items(courses) { course ->
                CourseItem(
                    course = course,
                    onUpdate = {
                        courseName = course.name
                        courseDescription = course.description
                        currentCourseId = course.id
                        isUpdating = true
                    },
                    onDelete = {
                        val success = databaseHelper.deleteCourse(course.id)
                        if (success) {
                            refreshCourses()
                        }
                    }
                )
            }
        }
    }
}



@Composable
fun CourseItem(
    course: Course,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = course.name,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = course.description,
            color = Color.Gray,
            fontSize = 12.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onUpdate,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                modifier = Modifier.height(36.dp)
            ) {
                Text("UPDATE")
            }

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                modifier = Modifier.height(36.dp)
            ) {
                Text("DELETE")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

