package Balance


import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureBalanceRouting() {
    routing {
        get("/balance") {
            val email = call.request.queryParameters["email"]
            if (email == null) {
                call.respond(HttpStatusCode.BadRequest, "Email is required")
                return@get
            }
            val balance = fetchBalanceByEmail(email) // возвращает BalanceDTO?
            if (balance == null) {
                call.respond(HttpStatusCode.NotFound, "Balance not found")
            } else {
                call.respond(balance) // Отправляем готовый DTO
            }
        }
    }
}


