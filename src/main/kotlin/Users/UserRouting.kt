package Users


import com.project.database.users.Users
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*


import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

// DTO для выдачи пользователю (без пароля)
@Serializable
data class UserResponseDTO(
    val email: String,
    val firstName: String,
    val secondName: String
)
@Serializable
data class UserUpdateRequest(
    val firstName: String,
    val secondName: String
)
fun Application.userRouting() {
routing {
    get("/users") {
        val users = transaction {
            Users.selectAll().map { row ->
                UserResponseDTO(
                    email = row[Users.email],
                    firstName = row[Users.firstName],
                    secondName = row[Users.secondName]
                )
            }
        }
        call.respond(users)

    }
    put("/users/{email}") {
        val email = call.parameters["email"]
        if (email == null) {
            call.respond(HttpStatusCode.BadRequest, "Email не указан")
            return@put
        }

        val updateRequest = call.receive<UserUpdateRequest>()
        val success = Users.updateUserProfile(email, updateRequest.firstName, updateRequest.secondName)

        if (success) {
            call.respond(HttpStatusCode.OK, "Профиль обновлен")
        } else {
            call.respond(HttpStatusCode.NotFound, "Пользователь не найден")
        }
    }
}
}

