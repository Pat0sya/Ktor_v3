package Balance



import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Balances : Table("balances") {
    val email = varchar("email", 255)
    val balance = double("balance")
}
@Serializable
data class BalanceDTO(val balance: Double)

fun fetchBalanceByEmail(email: String): BalanceDTO? = transaction {
    Balances.select { Balances.email eq email }
        .map { row ->
            BalanceDTO(balance = row[Balances.balance].toDouble())
        }.singleOrNull()
}
