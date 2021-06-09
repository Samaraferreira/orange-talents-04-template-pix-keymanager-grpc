package br.com.zup.edu.pix.register

import br.com.zup.edu.client.itau.ItauClient
import br.com.zup.edu.shared.exceptions.ClientNotFoundException
import br.com.zup.edu.shared.exceptions.PixKeyAlreadyExistsException
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class RegisterKeyService(val repository: PixKeyRepository, val itauClient: ItauClient) {

    @Transactional
    fun register(@Valid request: RegisterKeyRequest): PixKey {

        val response = itauClient.findAccount(request.clientId.toString(), request.accountType!!.name)
        val itauAccount = response.body()?.toModel()
                            ?: throw ClientNotFoundException("Client with id ${request.clientId} not found")

        if(repository.existsByKeyValue(request.keyValue)) {
            throw PixKeyAlreadyExistsException("Pix key already registered")
        }

        val pixKeyEntity = request.toModel(itauAccount)
        repository.save(pixKeyEntity)

        return pixKeyEntity
    }
}
