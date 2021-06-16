package br.com.zup.edu.pix.search

import br.com.zup.edu.pix.model.Account
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyType
import java.time.LocalDateTime
import java.util.UUID

data class PixKeyInfo(
    val pixId: UUID? = null,
    val clientId: UUID? = null,
    val keyType: PixKeyType,
    val keyValue: String,
    val account: Account,
    val createdAt: LocalDateTime
) {
    companion object {
        fun of(key: PixKey): PixKeyInfo {
            return PixKeyInfo(
                pixId = key.pixId,
                clientId = key.clientId,
                keyType = key.keyType,
                keyValue = key.keyValue,
                account = key.account,
                createdAt = key.createdAt
            )
        }
    }
}