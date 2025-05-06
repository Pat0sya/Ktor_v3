package com.project.login

import com.project.database.tokens.TokenDTO
import com.project.database.tokens.Tokens
import com.project.database.users.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.UUID

fun Application.configureLoginRouting() {
    routing {
        post("/login") {
            val receive = call.receive<LoginReceiveRemote>()

            // Проверка на пустые поля
            if (receive.email.isBlank() || receive.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Email и пароль не должны быть пустыми")
                return@post
            }

            // Проверка email формата
            if (!receive.email.contains("@") || !receive.email.contains(".")) {
                call.respond(HttpStatusCode.BadRequest, "Неверный формат почты")
                return@post
            }

            val userDTO = Users.fetchUser(receive.email)

            if (userDTO == null) {
                call.respond(HttpStatusCode.BadRequest, "Пользователь не найден")
            } else {
                if (userDTO.password == receive.password) {
                    val token = UUID.randomUUID().toString()
                    Tokens.insert(
                        TokenDTO(
                            rowId = UUID.randomUUID().toString(),
                            email = receive.email,
                            token = token
                        )
                    )
                    call.respond(LoginResponseRemote(token))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Неверный пароль")
                }
            }
        }
    }
}
