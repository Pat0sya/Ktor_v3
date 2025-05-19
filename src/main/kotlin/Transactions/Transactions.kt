package Transactions


import com.project.database.transactions.TransactionDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Transactions : Table("transactions") {
    val id = integer("id").autoIncrement()
    val userEmail = varchar("user_email", 50).references(com.project.database.users.Users.email)
    val amount = integer("amount") // может быть отрицательное или положительное число
    val description = varchar("description", 255)
    val timestamp = long("timestamp") // UNIX-время или можно `datetime("timestamp")` с DateTime
    override val primaryKey = PrimaryKey(id)
}

object TransactionDAO {

    fun insertTransaction(transactionDTO: TransactionDTO) {
        transaction {
            Transactions.insert {
                it[userEmail] = transactionDTO.userEmail
                it[amount] = transactionDTO.amount
                it[description] = transactionDTO.description
                it[timestamp] = transactionDTO.timestamp
            }
        }
    }

    fun fetchByEmail(email: String): List<TransactionDTO> {
        return transaction {
            Transactions.select { Transactions.userEmail eq email }
                .map {
                    TransactionDTO(
                        id = it[Transactions.id],
                        userEmail = it[Transactions.userEmail],
                        amount = it[Transactions.amount],
                        description = it[Transactions.description],
                        timestamp = it[Transactions.timestamp]
                    )
                }
        }
    }
}
