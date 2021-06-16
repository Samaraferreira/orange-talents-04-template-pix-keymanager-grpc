package br.com.zup.edu.pix.search

import br.com.zup.edu.AccountType
import br.com.zup.edu.GetKeyResponse
import br.com.zup.edu.KeyType
import com.google.protobuf.Timestamp
import java.time.ZoneId

class GetPixKeyResponseConverter {
    fun convert(keyInfo: PixKeyInfo): GetKeyResponse {
        return GetKeyResponse.newBuilder()
            .setClientId(keyInfo.clientId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixId(keyInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixKey(GetKeyResponse.PixKey
                .newBuilder()
                .setKeyType(KeyType.valueOf(keyInfo.keyType.name))
                .setKeyValue(keyInfo.keyValue)
                .setAccount(GetKeyResponse.PixKey.AccountInfo.newBuilder()
                    .setAccountType(AccountType.valueOf(keyInfo.account.accountType.name))
                    .setInstitutionName(keyInfo.account.institutionName)
                    .setClientName(keyInfo.account.clientName)
                    .setClientCpf(keyInfo.account.clientCpf)
                    .setAgencyNumber(keyInfo.account.agencyNumber)
                    .setAccountNumber(keyInfo.account.accountNumber)
                    .build()
                )
                .setCreatedAt(keyInfo.createdAt.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }
}
