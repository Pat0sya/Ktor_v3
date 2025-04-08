package com.example
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    // Передаем `this` как `Application`
    initDatabase(this)

    routing {
        get("/transactions") {
            val transactions = transaction {
                Transactions.selectAll().map {
                    Transaction(
                        it[Transactions.name],
                        it[Transactions.amount],
                        it[Transactions.date]
                    )
                }
            }
            call.respond(transactions)
        }
        post("/transactions") {
            val transaction = call.receive<Transaction>()
            transaction {
                Transactions.insert {
                    it[name] = transaction.name
                    it[amount] = transaction.amount
                    it[date] = transaction.date
                }
            }
            call.respond(HttpStatusCode.Created)
        }
    }

}

object Transactions : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val amount = varchar("amount", 50)
    val date = varchar("date", 100)

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Transaction(val name: String, val amount: String, val date: String)
fun initDatabase(app: Application) {
    // You can hard-code the database connection details here instead
    val url = "jdbc:postgresql://localhost:5432/transactions"
    val driver = "org.postgresql.Driver"
    val user = "postgres"
    val password = "postgres"

    Database.connect(url = url, driver = driver, user = user, password = password)

    transaction {
        SchemaUtils.create(Transactions)
    }
}
object ApiClient {

    private val client = HttpClient(CIO) {

    }

    suspend fun getTransactions(): List<Transaction> {
        return client.get("http://10.0.2.2:8080/transactions").body()
    }

    suspend fun addTransaction(transaction: Transaction) {
        client.post("http://10.0.2.2:8080/transactions") {
            contentType(ContentType.Application.Json)
            setBody(transaction)
        }
    }
}