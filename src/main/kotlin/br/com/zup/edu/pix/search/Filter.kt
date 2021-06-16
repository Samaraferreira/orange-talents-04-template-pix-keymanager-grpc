package br.com.zup.edu.pix.search

import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.shared.annotations.ValidUUID
import br.com.zup.edu.shared.exceptions.PixKeyNotFoundException
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filter {

    abstract fun run(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo

    @Introspected
    data class GetByPixId(
        @field:NotBlank @field:ValidUUID val clientId: String,
        @field:NotBlank @field:ValidUUID val pixId: String
    ) : Filter() {

        private fun pixIdAsUuid() = UUID.fromString(pixId)
        private fun clientIdAsUuid() = UUID.fromString(clientId)

        override fun run(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo {
            return repository.findByPixIdAndClientId(pixIdAsUuid(), clientIdAsUuid())
                                .map(PixKeyInfo::of)
                                .orElseThrow { PixKeyNotFoundException("Pix key not found") }
        }
    }

    @Introspected
    data class GetByKey(@field:NotBlank @Size(max = 77) val keyValue: String) : Filter() {

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun run(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo {
            return repository.findByKeyValue(keyValue)
                .map(PixKeyInfo::of)
                .orElseGet {
                    LOGGER.info("Consultando chave Pix '$keyValue' no Banco Central do Brasil (BCB)")

                    val response = bcbClient.findByKey(keyValue)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw PixKeyNotFoundException("Pix key not found")
                    }
                }
        }
    }

    @Introspected
    class Invalid : Filter() {
        override fun run(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo {
            throw IllegalArgumentException("Pix key invalid or not entered")
        }
    }

}