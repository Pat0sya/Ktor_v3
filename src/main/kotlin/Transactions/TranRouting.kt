package Transactions


import com.project.database.users.Users
import Balance.ensureBalanceExists
import Balance.fetchBalanceByEmail
import Balance.updateBalance
import com.project.database.transactions.TransactionDTO
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.log

fun Application.transactionRouting() {
    routing {
        post("/transaction") {
            try {
                val transactionRequest = call.receive<TransactionDTO>() // DTO, которое приходит с клиента

                // Сумма транзакции в копейках (Int)
                val amountInCents = transactionRequest.amount
                // Сумма транзакции в рублях (Double) для операций с балансом
                val amountInRubles = amountInCents / 100.0

                // 1. Валидация суммы
                if (amountInRubles <= 0) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Amount must be positive"))
                }

                // Начало атомарной транзакции базы данных
                org.jetbrains.exposed.sql.transactions.transaction {
                    // 2. Проверяем существование отправителя и получателя
                    val senderExists = Users.fetchUser(transactionRequest.senderEmail) != null
                    val recipientExists = Users.fetchUser(transactionRequest.recipientEmail) != null

                    if (!senderExists) {
                        throw IllegalStateException("Sender not found")
                    }
                    if (!recipientExists) {
                        throw IllegalStateException("Recipient not found")
                    }

                    // (Опционально) Убедимся, что записи о балансе существуют
                    // ensureBalanceExists(transactionRequest.senderEmail)
                    // ensureBalanceExists(transactionRequest.recipientEmail)


                    // 3. Получаем текущие балансы
                    val senderBalanceDTO = fetchBalanceByEmail(transactionRequest.senderEmail)
                    val recipientBalanceDTO = fetchBalanceByEmail(transactionRequest.recipientEmail)

                    if (senderBalanceDTO == null) {
                        // Это не должно произойти, если ensureBalanceExists отработал или баланс всегда есть
                        application.log.error("Sender balance record not found for ${transactionRequest.senderEmail}")
                        throw IllegalStateException("Sender balance information is missing.")
                    }
                    if (recipientBalanceDTO == null) {
                        application.log.error("Recipient balance record not found for ${transactionRequest.recipientEmail}, creating with 0.0")
                        // Если получатель может не иметь баланса, создаем его
                        ensureBalanceExists(transactionRequest.recipientEmail, 0.0)
                        // и получаем снова, или просто используем 0.0 как начальный баланс для него
                        val newlyCreatedRecipientBalanceDTO = fetchBalanceByEmail(transactionRequest.recipientEmail)
                        if (newlyCreatedRecipientBalanceDTO == null) {
                            // Если даже после создания не удалось получить, это серьезная проблема
                            throw IllegalStateException("Could not initialize recipient balance.")
                        }
                        // Продолжаем с newlyCreatedRecipientBalanceDTO
                        // Эта часть усложняет, проще требовать чтобы ensureBalanceExists был вызван ранее
                        // или обрабатывать это при регистрации пользователя.
                        // Для простоты, если recipientBalanceDTO == null и ensureBalanceExists не используется, можно бросить ошибку:
                        throw IllegalStateException("Recipient balance information is missing.")
                    }


                    val currentSenderBalance = senderBalanceDTO.balance
                    val currentRecipientBalance = recipientBalanceDTO.balance // Если null, то 0.0 или ошибка, см. выше

                    // 4. Проверяем достаточность средств
                    if (currentSenderBalance < amountInRubles) {
                        throw IllegalStateException("Insufficient funds for sender ${transactionRequest.senderEmail}")
                    }

                    // 5. Обновляем балансы
                    val newSenderBalance = currentSenderBalance - amountInRubles
                    val newRecipientBalance = currentRecipientBalance + amountInRubles

                    val senderUpdateSuccess = updateBalance(transactionRequest.senderEmail, newSenderBalance)
                    val recipientUpdateSuccess = updateBalance(transactionRequest.recipientEmail, newRecipientBalance)

                    if (!senderUpdateSuccess || !recipientUpdateSuccess) {
                        // Если по какой-то причине обновление не удалось (хотя в рамках транзакции это маловероятно без ошибки)
                        application.log.error("Failed to update balances during transaction. Sender: $senderUpdateSuccess, Recipient: $recipientUpdateSuccess")
                        throw IllegalStateException("Failed to update balances.")
                    }

                    // 6. Сохраняем запись о транзакции (твой существующий код)
                    // TransactionDTO уже содержит все нужные поля из transactionRequest
                    TransactionDAO.insertTransaction(transactionRequest) // Используем transactionRequest напрямую, т.к. его поля совпадают
                } // Конец атомарной транзакции базы данных. Если здесь возникнет Exception, все откатится.

                call.respond(HttpStatusCode.Created, mapOf("message" to "Transaction successful and saved"))

            } catch (e: IllegalStateException) { // Ожидаемые ошибки бизнес-логики
                application.log.warn("Transaction failed: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: kotlinx.serialization.SerializationException) { // Ошибка десериализации запроса
                application.log.error("Failed to deserialize transaction request: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid transaction data: ${e.message}"))
            }
            catch (e: Exception) { // Другие неожиданные ошибки
                application.log.error("Failed to process transaction", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to process transaction: ${e.message ?: "Unknown error"}"))
            }
        }

        get("/transactions") {
            val email = call.request.queryParameters["email"]
            if (email.isNullOrBlank()) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or empty email parameter"))
            }
            try {
                val userTransactions = TransactionDAO.fetchByEmail(email)
                call.respond(userTransactions)
            } catch (e: Exception) {
                application.log.error("Failed to fetch transactions for $email", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch transactions: ${e.message}"))
            }
        }
    }
}

