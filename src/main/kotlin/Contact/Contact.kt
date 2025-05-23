package Contact

import Balance.Balances
import com.project.database.users.UserDTO
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table

import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import com.project.database.users.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*

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
    fun deleteContact(ownerEmail: String, contactEmail: String): Boolean {
        return transaction {
            val deletedRowCount = Contacts.deleteWhere {
                (Contacts.ownerEmail eq ownerEmail) and (Contacts.contactEmail eq contactEmail)
            }
            deletedRowCount > 0 // Returns true if one or more rows were deleted
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


