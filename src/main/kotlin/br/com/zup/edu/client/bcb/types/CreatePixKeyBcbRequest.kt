package br.com.zup.edu.client.bcb.types

import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.toAccountTypeBcb

data class CreatePixKeyBcbRequest(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcb,
    val owner: OwnerBcb
) {
    companion object {
        fun of(pixKey: PixKey): CreatePixKeyBcbRequest {
            return with(pixKey) {
                CreatePixKeyBcbRequest(
                    keyType = keyType.toBcbType(),
                    key = keyValue,
                    bankAccount = BankAccountBcb(
                        participant = "60701190",
                        branch = account.agencyNumber,
                        accountNumber = account.accountNumber,
                        accountType = account.accountType.toAccountTypeBcb()
                    ),
                    owner = OwnerBcb(
                        type = OwnerType.NATURAL_PERSON,
                        name = account.clientName,
                        taxIdNumber = account.clientCpf
                    )
                )
            }
        }
    }
}
