package br.com.zup.edu.client.bcb

import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbRequest
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbResponse
import br.com.zup.edu.pix.model.PixKey
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.pix.url}")
interface BcbClient {

    @Post(value = "/pix/keys", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun registerKey(@Body request: CreatePixKeyBcbRequest): HttpResponse<CreatePixKeyBcbResponse>

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