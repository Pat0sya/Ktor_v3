package Contact

import kotlinx.serialization.Serializable

@Serializable
data class ContactDTO(
    val ownerEmail: String,
    val contactEmail: String
)
@Serializable
data class TopUpDTO(
    val email: String,
    val amount: Double
)
