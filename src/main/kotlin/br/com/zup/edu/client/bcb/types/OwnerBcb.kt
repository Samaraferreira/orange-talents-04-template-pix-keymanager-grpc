package br.com.zup.edu.client.bcb.types

data class OwnerBcb(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
)

enum class OwnerType {
    NATURAL_PERSON, LEGAL_PERSON
}