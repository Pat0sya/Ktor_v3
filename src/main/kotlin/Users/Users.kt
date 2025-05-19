package com.project.database.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table("users") {
    val firstName = varchar("firstname", 25)
    val secondName = varchar("secondname", 25)
    val email = varchar("email", 25)
    val password = varchar("password", 25)

    fun insert(userDTO: UserDTO) {
        transaction {
            Users.insert {
                it[email] = userDTO.email
                it[password] = userDTO.password
                it[firstName] = userDTO.firstName
                it[secondName] = userDTO.secondName
            }
        }
    }

    fun fetchUser(email: String): UserDTO? {
        return try {
            transaction {
                Users.select { Users.email eq email }.singleOrNull()?.let { row ->
                    UserDTO(
                        email = row[Users.email],
                        password = row[password],
                        firstName = row[firstName],
                        secondName = row[secondName]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

}