package br.com.zup.edu.pix.register

import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.client.bcb.types.BankAccountBcb
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbRequest
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbResponse
import br.com.zup.edu.client.bcb.types.KeyTypeBcb
import br.com.zup.edu.client.bcb.types.OwnerBcb
import br.com.zup.edu.client.itau.ItauClient
import br.com.zup.edu.shared.exceptions.ClientNotFoundException
import br.com.zup.edu.shared.exceptions.PixKeyAlreadyExistsException
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.pix.model.toAccountTypeBcb
import br.com.zup.edu.shared.handle.ErrorHandler
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class RegisterKeyService(
    val repository: PixKeyRepository,
    val itauClient: ItauClient,
    val bcbClient: BcbClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun register(@Valid request: RegisterKeyRequest): PixKey {

        val response = itauClient.findAccount(request.clientId.toString(), request.accountType!!.name)
        val itauAccount = response.body()?.toModel()
                            ?: throw ClientNotFoundException("Client with id ${request.clientId} not found")

        if(repository.existsByKeyValue(request.keyValue)) {
            throw PixKeyAlreadyExistsException("Pix key already registered")
        }

        try {
            val newPixKey = request.toModel(itauAccount)

            // Registrando no BCB
            val responseBcb = bcbClient.registerKey(CreatePixKeyBcbRequest.of(newPixKey))
            LOGGER.info("Pix key saved in BCB")

            newPixKey.updateKeyValue(responseBcb.body().key)
            repository.save(newPixKey)

            return newPixKey
        } catch (e: HttpClientResponseException) {
            if (e.status == HttpStatus.UNPROCESSABLE_ENTITY)
                throw PixKeyAlreadyExistsException("Pix key already registered")

            LOGGER.error("Failed to connect to the BCB server")
            throw IllegalStateException("Could not register pix key")
        }

//        val responseBcb = bcbClient.registerKey(CreatePixKeyBcbRequest.of(newPixKey))
//
//        if (responseBcb.status == HttpStatus.UNPROCESSABLE_ENTITY) {
//            throw PixKeyAlreadyExistsException("Pix key already registered")
//        }

//        if (responseBcb.status != HttpStatus.CREATED) {
//            throw IllegalStateException("Could not register pix key")
//        }
    }
}
