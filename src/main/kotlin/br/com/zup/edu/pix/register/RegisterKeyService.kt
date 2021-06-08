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

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun register(@Valid registerKeyRequest: RegisterKeyRequest): PixKey {

        // verifica se cliente existe no itau
        val response = itauClient.findAccount(registerKeyRequest.clientId.toString(), registerKeyRequest.accountType!!.name)
        val itauAccount = response.body()?.toModel()
                            ?: throw ClientNotFoundException("Client with id ${registerKeyRequest.clientId.toString()} not found")

        // verifica se a chave já foi cadastrada
        if(repository.existsByKeyValue(registerKeyRequest.keyValue)) {
            throw PixKeyAlreadyExistsException("Pix key already registered")
        }

        val pixKeyEntity = registerKeyRequest.toModel(itauAccount)
        repository.save(pixKeyEntity)

        return pixKeyEntity
    }
}
