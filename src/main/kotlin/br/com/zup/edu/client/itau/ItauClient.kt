package br.com.zup.edu.client.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.accounts.url}")
interface ItauClient {

    @Get("/clientes/{clienteId}/contas")
    fun findAccount(
        @PathVariable("clienteId") clientId: String,
        @QueryValue("tipo") type: String
    ) : HttpResponse<ItauAccountResponse>
}