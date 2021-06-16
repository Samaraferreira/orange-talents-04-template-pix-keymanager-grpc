package br.com.zup.edu.pix.list

import br.com.zup.edu.AccountType
import br.com.zup.edu.KeyManagerGetAllServiceGrpc
import br.com.zup.edu.KeyType
import br.com.zup.edu.ListAllKeysRequest
import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.pix.model.Account
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.pix.model.PixKeyType
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListKeysEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: KeyManagerGetAllServiceGrpc.KeyManagerGetAllServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @Test
    fun `should load all client keys`() {
        // cenário
        repository.save(newPixKey(PixKeyType.EMAIL, "random@email.com", CLIENT_ID))
        repository.save(newPixKey(PixKeyType.PHONE_NUMBER, "+5585988714077", CLIENT_ID))
        // ação

        val response = grpcClient.getAll(ListAllKeysRequest.newBuilder()
                                        .setClientId(CLIENT_ID.toString())
                                        .build())
        // validação
        with(response.keysList) {
            assertThat(this, hasSize(2))
            assertThat(
                this.map { Pair(it.keyType, it.keyValue) }.toList(),
                containsInAnyOrder(
                    Pair(KeyType.EMAIL, "random@email.com"),
                    Pair(KeyType.PHONE_NUMBER, "+5585988714077")
                )
            )
        }
    }

    @Test
    fun `should not load keys if the client does not have registered keys`() {
        val newClientId = UUID.randomUUID()

        val response = grpcClient.getAll(
            ListAllKeysRequest.newBuilder()
                .setClientId(newClientId.toString())
                .build()
        )

        assertEquals(response.keysList.size, 0)
    }

    @Test
    fun `should not load keys if clientId is invalid`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.getAll(
                ListAllKeysRequest.newBuilder()
                    .setClientId("")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("ClientId must not be blank or null", status.description)
        }
    }

    private fun newPixKey(
        keyType: PixKeyType,
        keyValue: String = UUID.randomUUID().toString(),
        clientId: UUID = UUID.randomUUID(),
    ): PixKey {
        return PixKey(
            clientId = clientId,
            keyType = keyType,
            keyValue = keyValue,
            account = Account(
                accountType = AccountType.CONTA_CORRENTE,
                institutionName = "UNIBANCO ITAU",
                clientName = "Rafael Ponte",
                clientCpf = "12345678900",
                agencyNumber = "1218",
                accountNumber = "123456"
            )
        )
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGetAllServiceGrpc.KeyManagerGetAllServiceBlockingStub? {
            return KeyManagerGetAllServiceGrpc.newBlockingStub(channel)
        }
    }
}
