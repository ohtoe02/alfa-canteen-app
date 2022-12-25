package com.example.canteenapp.utils.models

data class ClassData(
    val id: String,
    val title: String,
    val studentsCount: Int,
    val headTeacher: String,
    val students: HashMap<String, String>
) : java.io.Serializable
