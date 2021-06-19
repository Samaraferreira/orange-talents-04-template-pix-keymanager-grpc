package br.com.zup.edu

import br.com.zup.edu.pix.register.RegisterKeyRequest
import br.com.zup.edu.pix.model.PixKeyType
import br.com.zup.edu.GetKeyRequest.FilterCase.*
import br.com.zup.edu.pix.search.Filter
import java.util.UUID

fun CreateKeyRequest.toModel(): RegisterKeyRequest {
    return RegisterKeyRequest(
        clientId = UUID.fromString(clientId),
        keyValue = keyValue,
        keyType = when(keyType) {
            KeyType.UNKNOWN_KEY_TYPE -> null
            else -> PixKeyType.valueOf(keyType.name)
        },
        accountType = when(accountType) {
            AccountType.UNKNOWN_ACCOUNT_TYPE -> null
            else -> AccountType.valueOf(accountType.name)
        }
    )
}