package br.com.zup.edu.pix.remove

import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.shared.exceptions.PixKeyNotFoundException
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class RemoveKeyService(val repository: PixKeyRepository) {

    @Transactional
    fun remove(@Valid request: RemoveKeyRequest) {

        val pixId = request.pixId
        val clientId = request.clientId

        val pixKey = repository.findByPixIdAndClientId(pixId, clientId).orElseThrow {
            throw PixKeyNotFoundException("Pix key not found")
        }

        repository.delete(pixKey)
    }
}
