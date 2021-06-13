package br.com.zup.edu.pix.register

import br.com.zup.edu.AccountType
import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.KeyManagerRegisterServiceGrpc
import br.com.zup.edu.KeyType
import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.client.bcb.types.AccountTypeBcb
import br.com.zup.edu.client.bcb.types.BankAccountBcb
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbRequest
import br.com.zup.edu.client.bcb.types.CreatePixKeyBcbResponse
import br.com.zup.edu.client.bcb.types.KeyTypeBcb
import br.com.zup.edu.client.bcb.types.OwnerBcb
import br.com.zup.edu.client.bcb.types.OwnerType
import br.com.zup.edu.client.itau.AccountOwnerResponse
import br.com.zup.edu.client.itau.InstitutionResponse
import br.com.zup.edu.client.itau.ItauAccountResponse
import br.com.zup.edu.client.itau.ItauClient
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.pix.model.PixKeyType
import br.com.zup.edu.toModel
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
import org.junit.jupiter.api.Assertions.assertNotNull
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
class RegisterKeyEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

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

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Test
    fun `should create a new pix key`() {
        val fakePixkey = createFakePixKey()

        Mockito
            .`when`(itauClient.findAccount(CLIENT_ID.toString(), AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(createFakeItauResponse()))

        Mockito
            .`when`(bcbClient.registerKey(CreatePixKeyBcbRequest.of(fakePixkey)))
            .thenReturn(HttpResponse.created(createPixKeyBcbResponse(fakePixkey)))

        val response = grpcClient.register(createFakeKeyRequest())

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByPixId(UUID.fromString(pixId))) // efeito colateral
        }
    }

    @Test
    fun `should not register pix key if BCB server returns error`() {
        val fakePixkey = createFakePixKey()

        Mockito
            .`when`(itauClient.findAccount(CLIENT_ID.toString(), AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(createFakeItauResponse()))

        Mockito
            .`when`(bcbClient.registerKey(CreatePixKeyBcbRequest.of(fakePixkey)))
            .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.register(createFakeKeyRequest())
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Could not register pix key", status.description)
        }
        assertFalse(repository.existsByPixId(fakePixkey.pixId))
    }

    @Test
    fun `should throw ConstraintViolationException if parameters is invalid`() {
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
    fun `should throw ClientNotFoundException if account not found`() {
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
    fun `should throw PixKeyAlreadyExistsException if key already registered`() {
        Mockito
            .`when`(itauClient.findAccount(CLIENT_ID.toString(), "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(createFakeItauResponse()))

        repository.save(createFakePixKey())

        val request = createFakeKeyRequest()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.register(request)
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Pix key already registered", status.description)
        }
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

    private fun createFakeKeyRequest(): CreateKeyRequest {
        return CreateKeyRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setKeyType(KeyType.EMAIL)
            .setKeyValue("maria@test.com")
            .build()
    }

    private fun createFakePixKey(): PixKey {
        val fakeRequest = createFakeKeyRequest().toModel()
        return PixKey(
            clientId = fakeRequest.clientId,
            keyValue = fakeRequest.keyValue,
            keyType = fakeRequest.keyType!!,
            accountType = fakeRequest.accountType!!,
            account = createFakeItauResponse().toModel()
        )
    }

    private fun createFakeItauResponse(): ItauAccountResponse {
        return ItauAccountResponse(
            number = "123456",
            agency = "4443",
            type = AccountType.CONTA_CORRENTE.toString(),
            owner = AccountOwnerResponse(
                CLIENT_ID.toString(),
                "Maria Test",
                "74304413007"
            ),
            institution = InstitutionResponse("ITAÚ UNIBANCO S.A.", "60701190")
        )
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceBlockingStub? {
            return KeyManagerRegisterServiceGrpc.newBlockingStub(channel);
        }
    }

}