package br.com.zup.edu.pix.register

import br.com.zup.edu.AccountType
import br.com.zup.edu.pix.model.Account
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyType
import br.com.zup.edu.shared.annotations.ValidPixKey
import br.com.zup.edu.shared.annotations.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidPixKey
data class RegisterKeyRequest(@field:NotNull @ValidUUID val clientId: UUID,
                              @field:Size(max = 77) val keyValue: String,
                              @field:NotNull val keyType: PixKeyType?,
                              @field:NotNull val accountType: AccountType?
) {
    fun toModel(account: Account): PixKey {
        return PixKey(
            clientId = clientId,
            keyValue = if (keyType == PixKeyType.RANDOM_KEY) UUID.randomUUID().toString() else this.keyValue,
            keyType = keyType!!,
            accountType = accountType!!,
            account = account
        )
    }
}