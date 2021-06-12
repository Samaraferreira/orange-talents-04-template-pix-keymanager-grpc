package br.com.zup.edu.client.bcb.types

import java.time.LocalDateTime

data class CreatePixKeyBcbResponse(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcb,
    val owner: OwnerBcb,
    val createdAt: LocalDateTime
)
