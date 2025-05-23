package Contact

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*


fun Application.contactAndBalanceRouting() {
    routing {


        post("/contacts/add") {
            val request = call.receive<ContactDTO>()
            if (request.ownerEmail == request.contactEmail) {
                call.respond(HttpStatusCode.BadRequest, "Нельзя добавить самого себя")
                return@post
            }
            ContactDAO.addContact(request.ownerEmail, request.contactEmail)
            call.respond(HttpStatusCode.OK, "Пользователь добавлен в контакты")
        }
        delete("/contacts/remove") { // Matches the client's URL
            try {
                val request = call.receive<ContactDTO>() // Or use ContactDTO if structure matches
                // Basic validation
                if (request.ownerEmail.isBlank() || request.contactEmail.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "ownerEmail and contactEmail cannot be blank.")
                    return@delete
                }

                val success = ContactDAO.deleteContact(request.ownerEmail, request.contactEmail)

                if (success) {
                    call.respond(HttpStatusCode.OK, "Контакт успешно удален")
                } else {
                    // This could mean the contact was not found to be deleted,
                    // or some other DB error. 404 might be appropriate if the specific contact link didn't exist.
                    call.respond(HttpStatusCode.NotFound, "Контакт не найден или не удалось удалить")
                }
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: Exception) {
                application.log.error("Error deleting contact: ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере при удалении контакта")
            }
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
