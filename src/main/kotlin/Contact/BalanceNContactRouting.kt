package Contact

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*


fun Application.contactAndBalanceRouting() {
    routing {

        // ✅ Добавление пользователя в контакты
        post("/contacts/add") {
            val request = call.receive<ContactDTO>()
            if (request.ownerEmail == request.contactEmail) {
                call.respond(HttpStatusCode.BadRequest, "Нельзя добавить самого себя")
                return@post
            }
            ContactDAO.addContact(request.ownerEmail, request.contactEmail)
            call.respond(HttpStatusCode.OK, "Пользователь добавлен в контакты")
        }

        // ✅ Получение списка контактов пользователя
        get("/contacts") {
            val ownerEmail = call.request.queryParameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing email")
            val contacts = ContactDAO.getContacts(ownerEmail)
            call.respond(contacts)
        }

        // ✅ Пополнение баланса
        post("/topup") {
            val request = call.receive<TopUpDTO>()
            BalanceDAO.topUpBalance(request.email, request.amount)
            call.respond(HttpStatusCode.OK, "Balance topped up")

        }

    }
}
