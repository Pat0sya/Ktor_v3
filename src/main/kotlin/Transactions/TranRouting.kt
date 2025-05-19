package Transactions



import com.project.database.transactions.TransactionDTO
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.Application

fun Application.transactionRouting() {
routing {
    post("/transaction") {
        val transaction = call.receive<TransactionDTO>()
        TransactionDAO.insertTransaction(transaction)
        call.respond(HttpStatusCode.OK, "Transaction saved")
    }

    get("/transactions") {
        val email = call.request.queryParameters["email"] ?: return@get call.respondText("Missing email", status = HttpStatusCode.BadRequest)
        val userTransactions = TransactionDAO.fetchByEmail(email)
        call.respond(userTransactions)
    }

}
}
