package com.project.registration

import com.project.database.tokens.TokenDTO
import com.project.database.tokens.Tokens
import com.project.database.users.UserDTO
import com.project.database.users.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.UUID

fun Application.configureRegistrationRouting() {
    routing {
        post("/registration") {
            val receive = call.receive<RegistrationReceiveRemote>()

            // Проверка на пустые поля
            if (receive.email.isBlank() || receive.password.isBlank() ||
                receive.firstName.isBlank() || receive.secondName.isBlank()
            ) {
                call.respond(HttpStatusCode.BadRequest, "Все поля обязательны для заполнения")
                return@post
            }

            // Проверка формата email
            val emailRegex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$".toRegex()
            if (!receive.email.matches(emailRegex)) {
                call.respond(HttpStatusCode.BadRequest, "Неверный формат почты")
                return@post
            }

            val userDTO = Users.fetchUser(receive.email)
            if (userDTO != null) {
                call.respond(HttpStatusCode.Conflict, "Пользователь уже существует")
            } else {
                val token = UUID.randomUUID().toString()
                try {
                    Users.insert(
                        UserDTO(
                            email = receive.email,
                            password = receive.password,
                            firstName = receive.firstName,
                            secondName = receive.secondName
                        )
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, e.toString())
                    return@post
                }

                Tokens.insert(
                    TokenDTO(
                        rowId = UUID.randomUUID().toString(),
                        email = receive.email,
                        token = token
                    )
                )
                call.respond(RegistrationResponseRemote(token))
            }
        }
    }
}