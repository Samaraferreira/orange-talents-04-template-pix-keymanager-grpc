package br.com.zup.edu.client.bcb

import br.com.zup.edu.AccountType
import br.com.zup.edu.client.bcb.types.AccountTypeBcb
import br.com.zup.edu.client.bcb.types.BankAccountBcb
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbRequest
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbResponse
import br.com.zup.edu.client.bcb.types.KeyTypeBcb
import br.com.zup.edu.client.bcb.types.OwnerBcb
import br.com.zup.edu.pix.Institutions
import br.com.zup.edu.pix.model.Account
import br.com.zup.edu.pix.search.PixKeyInfo
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.pix.url}")
interface BcbClient {

    @Post(value = "/pix/keys", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun registerKey(@Body request: CreatePixKeyBcbRequest): HttpResponse<CreatePixKeyBcbResponse>

    @Get(value = "/pix/keys/{key}", consumes = [MediaType.APPLICATION_XML])
    fun findByKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

    @Delete(value = "/pix/keys/{key}", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun deleteKey(@PathVariable key: String, @Body request: DeletePixKeyBcdRequest): HttpResponse<DeletePixKeyBcdResponse>
}

data class DeletePixKeyBcdRequest(
    val key: String,
    val participant: String = "60701190"
)

data class DeletePixKeyBcdResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class PixKeyDetailsResponse (
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcb,
    val owner: OwnerBcb,
    val createdAt: LocalDateTime
) {

    fun toModel(): PixKeyInfo {
        return PixKeyInfo(
            keyType = keyType.toDomainType(),
            keyValue = key,
            account = Account(
                accountType = when (this.bankAccount.accountType) {
                    AccountTypeBcb.CACC -> AccountType.CONTA_CORRENTE
                    AccountTypeBcb.SVGS -> AccountType.CONTA_POUPANCA
                },
                institutionName = Institutions.name(bankAccount.participant),
                clientName = owner.name,
                clientCpf = owner.taxIdNumber,
                agencyNumber = bankAccount.branch,
                accountNumber = bankAccount.accountNumber
            ),
            createdAt = createdAt
        )
    }
}
