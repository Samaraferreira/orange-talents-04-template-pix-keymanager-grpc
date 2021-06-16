package br.com.zup.edu.pix.remove

import br.com.zup.edu.AccountType
import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.KeyManagerDeleteServiceGrpc
import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.client.bcb.DeletePixKeyBcdRequest
import br.com.zup.edu.client.bcb.DeletePixKeyBcdResponse
import br.com.zup.edu.client.bcb.types.AccountTypeBcb
import br.com.zup.edu.client.bcb.types.BankAccountBcb
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbRequest
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbResponse
import br.com.zup.edu.client.bcb.types.OwnerBcb
import br.com.zup.edu.client.bcb.types.OwnerType
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
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveKeyEndpointTest(
    private val grpcClient: KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub,
    private val repository: PixKeyRepository
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
        val KEY_VALUE: UUID = UUID.randomUUID()
    }

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Test
    fun `should remove a pix key`() {
        val pixKey = createFakePixKey()

        Mockito
            .`when`(bcbClient.deleteKey(pixKey.keyValue, DeletePixKeyBcdRequest(pixKey.keyValue)))
            .thenReturn(HttpResponse.ok())

        repository.save(pixKey)

        val response = grpcClient.delete(DeleteKeyRequest.newBuilder()
                                                .setPixId(pixKey.pixId.toString())
                                                .setClientId(CLIENT_ID.toString())
                                                .build())

        assertEquals(pixKey.pixId.toString(), response.pixId)
        assertFalse(repository.existsByPixId(UUID.fromString(response.pixId))) // efeito colateral
    }

    @Test
    fun `should not remove pix key if key not exists`() {

        val randomPixID = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeleteKeyRequest.newBuilder()
                .setPixId(randomPixID)
                .setClientId(CLIENT_ID.toString())
                .build())
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix key not found", status.description)
        }
    }

    @Test
    fun `should not remove pix key if client is not owner`() {
        val pixKey = createFakePixKey()
        repository.save(pixKey)

        val randomClientID = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeleteKeyRequest.newBuilder()
                .setPixId(pixKey.pixId.toString())
                .setClientId(randomClientID)
                .build())
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix key not found", status.description)
        }
    }

    @Test
    fun `should throw ConstraintViolationException if parameters format is invalid`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeleteKeyRequest.newBuilder()
                .setPixId("dhsgdggdgdg")
                .setClientId("dhsgdggdgdg")
                .build())
        }

        assertEquals(Status.INVALID_ARGUMENT.code, thrown.status.code)
    }

    @Test
    fun `should not remove pix key if BCB server returns error`() {
        val pixKey = createFakePixKey()

        Mockito
            .`when`(bcbClient.deleteKey(pixKey.keyValue, DeletePixKeyBcdRequest(pixKey.keyValue)))
            .thenReturn(HttpResponse.unprocessableEntity())

        repository.save(pixKey)

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeleteKeyRequest.newBuilder()
                .setPixId(pixKey.pixId.toString())
                .setClientId(CLIENT_ID.toString())
                .build())
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Could not remove pix key", status.description)
        }

        assertTrue(repository.existsByPixId(pixKey.pixId))
    }

    private fun createFakePixKey(): PixKey {
        return PixKey(
            clientId = CLIENT_ID,
            keyValue = KEY_VALUE.toString(),
            keyType = PixKeyType.RANDOM_KEY,
            account = createFakeAccount()
        )
    }

    private fun createFakeAccount(): Account {
        return Account(
            accountType = AccountType.CONTA_CORRENTE,
            accountNumber = "123456",
            agencyNumber = "4443",
            clientName = "Maria Test",
            clientCpf = "74304413007",
            institutionName = "ITAÚ UNIBANCO S.A.",
        )
    }

    private fun createPixKeyBcbResponse(fakePixkey: PixKey): CreatePixKeyBcbResponse {
        return CreatePixKeyBcbResponse(
            keyType = fakePixkey.keyType.toBcbType(),
            key = fakePixkey.keyValue,
            bankAccount = BankAccountBcb(
                participant = "60701190",
                branch = fakePixkey.account.agencyNumber,
                accountNumber = fakePixkey.account.accountNumber,
                accountType = AccountTypeBcb.CACC
            ),
            owner = OwnerBcb(
                type = OwnerType.NATURAL_PERSON,
                name = fakePixkey.account.clientName,
                taxIdNumber = fakePixkey.account.clientCpf
            ),
            createdAt = LocalDateTime.parse("2021-06-07T05:53:30.278166")
        )
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub? {
            return KeyManagerDeleteServiceGrpc.newBlockingStub(channel);
        }
    }

}
