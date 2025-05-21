package Transactions


import com.project.database.transactions.TransactionDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Transactions : Table("transactions") {

    val senderEmail = varchar("sender_email", 50).references(com.project.database.users.Users.email)
    val recipientEmail = varchar("recipient_email", 50).references(com.project.database.users.Users.email)
    val amount = integer("amount")
    val description = varchar("description", 255)
    val timestamp = long("timestamp")

}

object TransactionDAO {

    fun insertTransaction(transactionDTO: TransactionDTO) {
        transaction {
            Transactions.insert {
                it[senderEmail] = transactionDTO.senderEmail
                it[recipientEmail] = transactionDTO.recipientEmail
                it[amount] = transactionDTO.amount
                it[description] = transactionDTO.description
                it[timestamp] = transactionDTO.timestamp
            }
        }
    }


    fun fetchByEmail(email: String): List<TransactionDTO> {
        return transaction {
            Transactions.select { (Transactions.senderEmail eq email) or (Transactions.recipientEmail eq email) }
                .map {
                    TransactionDTO(
                        // Removed 'id' because it was causing NullPointerException
                        senderEmail = it[Transactions.senderEmail], // Issue here
                        recipientEmail  = it[Transactions.recipientEmail], // Issue here
                        amount = it[Transactions.amount],
                        description = it[Transactions.description],
                        timestamp = it[Transactions.timestamp]
                    )
                }
        }
    }
}
