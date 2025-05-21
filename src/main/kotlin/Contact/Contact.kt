package Contact

import Balance.Balances
import com.project.database.users.UserDTO
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.decimalLiteral
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import com.project.database.users.Users
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus

object Contacts : Table("contacts") {
    val ownerEmail = varchar("owner_email", 100).references(Users.email)
    val contactEmail = varchar("contact_email", 100).references(Users.email)
    override val primaryKey = PrimaryKey(ownerEmail, contactEmail)
}
object ContactDAO {
    fun addContact(ownerEmail: String, contactEmail: String) {
        transaction {
            Contacts.insertIgnore {
                it[Contacts.ownerEmail] = ownerEmail
                it[Contacts.contactEmail] = contactEmail
            }
        }
    }

    fun getContacts(ownerEmail: String): List<UserDTO> {
        return transaction {
            Contacts.join(
                Users,
                JoinType.INNER,
                additionalConstraint = { Contacts.contactEmail eq Users.email }
            )
                .slice(Users.email, Users.firstName, Users.secondName)
                .select { Contacts.ownerEmail eq ownerEmail }
                .map {
                    UserDTO(
                        email = it[Users.email],
                        password = "", // или null, если не нужен
                        firstName = it[Users.firstName],
                        secondName = it[Users.secondName]
                    )
                }
        }
    }


}

object BalanceDAO {
    fun topUpBalance(email: String, amount: Double) {
        transaction {
            Balances.update({ Balances.email eq email }) {
                with(SqlExpressionBuilder) {
                    val addAmount = doubleLiteral(amount)
                    it[balance] = balance + addAmount
                }
            }
        }
    }
}


