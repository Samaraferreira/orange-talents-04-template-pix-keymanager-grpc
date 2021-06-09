package br.com.zup.edu.pix.register

import br.com.zup.edu.AccountType
import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.KeyType
import br.com.zup.edu.client.itau.AccountOwnerResponse
import br.com.zup.edu.client.itau.InstitutionResponse
import br.com.zup.edu.client.itau.ItauAccountResponse
import br.com.zup.edu.client.itau.ItauClient
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.pix.model.PixKeyType
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
class RegisterKeyEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @Test
    fun `should be create a new pix key`() {
        Mockito
            .`when`(itauClient.findAccount(CLIENT_ID.toString(), "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(crateFakeItauResponse()))

        val response = grpcClient.register(createFakeKeyRequest())

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByPixId(UUID.fromString(pixId))) // efeito colateral
        }
    }

    @Test
    fun `should throw ConstraintViolationException when parameters is invalid`() {
        val invalidRequest = CreateKeyRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setAccountType(AccountType.UNKNOWN_ACCOUNT_TYPE)
            .setKeyType(KeyType.UNKNOWN_KEY_TYPE)
            .setKeyValue("")
            .build()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.register(invalidRequest)
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `should throw ClientNotFoundException when account not found`() {
        Mockito
            .`when`(itauClient.findAccount(CLIENT_ID.toString(), "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.register(createFakeKeyRequest())
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Client with id $CLIENT_ID not found", status.description)
        }
    }

    @Test
    fun `should throw PixKeyAlreadyExistsException when key already registered`() {
        Mockito
            .`when`(itauClient.findAccount(CLIENT_ID.toString(), "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(crateFakeItauResponse()))

        repository.save(creteFakePixKey())

        val request = createFakeKeyRequest()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.register(request)
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Pix key already registered", status.description)
        }
    }

    private fun createFakeKeyRequest(): CreateKeyRequest {
        return CreateKeyRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setKeyType(KeyType.EMAIL)
            .setKeyValue("maria@test.com")
            .build()
    }

    private fun creteFakePixKey(): PixKey {
        return PixKey(
            CLIENT_ID,
            "maria@test.com",
            PixKeyType.EMAIL,
            AccountType.CONTA_CORRENTE,
            crateFakeItauResponse().toModel()
        )
    }

    private fun crateFakeItauResponse(): ItauAccountResponse {
        return ItauAccountResponse(
            "123456",
            "4443",
            "CONTA_CORRENTE",
            AccountOwnerResponse(
                CLIENT_ID.toString(),
                "Maria Test",
                "74304413007"
            ),
            InstitutionResponse("ITAÚ UNIBANCO S.A.", "60701190")
        )
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel);
        }
    }

}