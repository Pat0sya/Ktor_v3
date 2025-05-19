package com.project.users

import com.project.database.tokens.Tokens
import com.project.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureUserRouting() {
    routing {
        get("/user/profile") {
            val email = call.request.queryParameters["email"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing email")
                return@get
            }

            val user = Users.fetchUser(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(mapOf("firstName" to user.firstName, "secondName" to user.secondName))
            }
        }
        }

}
