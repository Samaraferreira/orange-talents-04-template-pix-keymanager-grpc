package br.com.zup.edu.pix.remove

import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.client.bcb.DeletePixKeyBcdRequest
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.shared.annotations.ValidUUID
import br.com.zup.edu.shared.exceptions.PixKeyNotFoundException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.UUID
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotNull

@Singleton
@Validated
class RemoveKeyService(val repository: PixKeyRepository, val bcbClient: BcbClient) {

    @Transactional
    fun remove(@NotNull @ValidUUID("Client ID format is invalid") clientId: String?,
               @NotNull @ValidUUID("Pix ID format is invalid") pixId: String?
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClientId = UUID.fromString(clientId)

        val pixKey = repository.findByPixIdAndClientId(uuidPixId, uuidClientId).orElseThrow {
            throw PixKeyNotFoundException("Pix key not found")
        }

        val bcbResponse = bcbClient.deleteKey(pixKey.keyValue, DeletePixKeyBcdRequest(pixKey.keyValue))

        if (bcbResponse.status != HttpStatus.OK) {
            throw IllegalStateException("Could not remove pix key")
        }

        repository.delete(pixKey)
    }
}
