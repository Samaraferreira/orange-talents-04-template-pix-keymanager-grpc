package br.com.zup.edu.client.itau

import br.com.zup.edu.pix.model.Account
import com.fasterxml.jackson.annotation.JsonProperty

data class ItauAccountResponse(
    @JsonProperty("numero")
    val number: String,
    @JsonProperty("agencia")
    val agency: String,
    @JsonProperty("tipo")
    val type: String,
    @JsonProperty("titular")
    val owner: AccountOwnerResponse,
    @JsonProperty("instituicao")
    val institution: InstitutionResponse
) {
    fun toModel(): Account {
        return Account(
            accountNumber = number,
            agencyNumber = agency,
            clientName = owner.name,
            clientCpf = owner.cpf,
            institutionName = institution.name
        )
    }
}

data class AccountOwnerResponse(val id: String, @JsonProperty("nome") val name: String, val cpf: String)
data class InstitutionResponse(@JsonProperty("nome") val name: String, val ispb: String)