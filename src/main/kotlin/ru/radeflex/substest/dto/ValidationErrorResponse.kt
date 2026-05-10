package ru.radeflex.substest.dto

data class ValidationErrorResponse(
    val errors: Map<String, String?>,
)