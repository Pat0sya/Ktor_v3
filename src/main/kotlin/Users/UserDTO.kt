package com.project.database.users

import kotlinx.serialization.Serializable

@Serializable
class UserDTO (
    val email: String,
    val password: String,
    val firstName: String,
    val secondName: String
)