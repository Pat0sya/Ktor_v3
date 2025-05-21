package Balance



import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object Balances : Table("balances") {
    val email = varchar("email", 255) // Рекомендую .references(Users.email) и .primaryKey()
    val balance = double("balance")
    // override val primaryKey = PrimaryKey(email) // Если делаешь email первичным ключом
}

// Твой существующий BalanceDTO
@Serializable
data class BalanceDTO(val balance: Double)

// Твоя существующая функция fetchBalanceByEmail
fun fetchBalanceByEmail(email: String): BalanceDTO? = transaction {
    Balances.select { Balances.email eq email }
        .map { row ->
            BalanceDTO(balance = row[Balances.balance]) // .toDouble() не нужно, если тип колонки уже double
        }.singleOrNull()
}

// Новая функция для обновления баланса
fun updateBalance(userEmail: String, newBalance: Double): Boolean {
    return transaction {
        val updatedRows = Balances.update({ Balances.email eq userEmail }) {
            it[balance] = newBalance
        }
        updatedRows > 0 // Возвращает true, если строка была обновлена
    }
}

// (Опционально) Функция для создания начального баланса, если его нет
// Может понадобиться при регистрации пользователя или первой транзакции для нового получателя
fun ensureBalanceExists(userEmail: String, initialBalance: Double = 0.0) {
    transaction {
        val existing = Balances.select { Balances.email eq userEmail }.singleOrNull()
        if (existing == null) {
            Balances.insert {
                it[email] = userEmail
                it[balance] = initialBalance
            }
        }
    }
}
