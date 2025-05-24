package com.project.users

import Users.UserResponseDTO
import com.project.database.tokens.Tokens
import com.project.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jdk.internal.net.http.common.Log
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureUserRouting() {
    routing {
        get("/user/profile") {
            val email = call.request.queryParameters["email"]
            if (email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Параметр email отсутствует или пуст")
                return@get
            }

            // Используем твою существующую функцию для получения UserDTO (который может содержать пароль)
            val userWithPassword = Users.fetchUser(email) // Эта функция из твоего Users object

            if (userWithPassword == null) {
                call.respond(HttpStatusCode.NotFound, "Пользователь с email '$email' не найден")
            } else {
                // Создаем DTO для ответа, НЕ ВКЛЮЧАЯ ПАРОЛЬ
                val userProfileResponse = UserResponseDTO(
                    email = userWithPassword.email,
                    firstName = userWithPassword.firstName,
                    secondName = userWithPassword.secondName
                    // Не добавляй сюда userWithPassword.password!
                )
                call.respond(HttpStatusCode.OK, userProfileResponse)
            }
        }

        // Здесь могут быть другие маршруты, например, для обновления профиля POST /user/profile
        post("/user/profile/update") {
            try {
                val updateRequest = call.receive<UserResponseDTO>()
                val success = Users.updateUserProfile(
                    email = updateRequest.email,
                    firstName = updateRequest.firstName,
                    secondName = updateRequest.secondName
                )
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Профиль успешно обновлен"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Пользователь не найден"))
                }
            } catch (e: Exception) {
                application.log.error("Ошибка обновления профиля", e)
                call.respond(HttpStatusCode.BadRequest, "Ошибка в данных запроса: ${e.message}")
            }
        }
        }

}
