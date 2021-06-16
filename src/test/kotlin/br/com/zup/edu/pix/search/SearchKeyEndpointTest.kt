package br.com.zup.edu.pix.search

import br.com.zup.edu.AccountType
import br.com.zup.edu.GetKeyRequest
import br.com.zup.edu.GetKeyRequest.FilterByPixId
import br.com.zup.edu.KeyManagerGetServiceGrpc
import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.client.bcb.PixKeyDetailsResponse
import br.com.zup.edu.client.bcb.types.AccountTypeBcb
import br.com.zup.edu.client.bcb.types.BankAccountBcb
import br.com.zup.edu.client.bcb.types.KeyTypeBcb
import br.com.zup.edu.client.bcb.types.OwnerBcb
import br.com.zup.edu.client.bcb.types.OwnerType
import br.com.zup.edu.pix.model.Account
import br.com.zup.edu.pix.model.PixKey
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.pix.model.PixKeyType
import br.com.zup.edu.utils.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class SearchKeyEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: KeyManagerGetServiceGrpc.KeyManagerGetServiceBlockingStub,
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @BeforeEach
    fun setup() {
        repository.save(newPixKey(keyType = PixKeyType.EMAIL, keyValue = "rafael.ponte@zup.com.br", clientId = CLIENTE_ID))
        repository.save(newPixKey(keyType = PixKeyType.CPF, keyValue = "63657520325", clientId = UUID.randomUUID()))
        repository.save(newPixKey(keyType = PixKeyType.RANDOM_KEY, keyValue = "randomkey-3", clientId = CLIENTE_ID))
        repository.save(newPixKey(keyType = PixKeyType.PHONE_NUMBER, keyValue = "+551155554321", clientId = CLIENTE_ID))
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `should load Pix key by pixId and clientId`() {
        // cenário
        val chaveExistente = repository.findByKeyValue("+551155554321").get()

        // ação
        val response = grpcClient.get(
            GetKeyRequest.newBuilder()
            .setPixId(FilterByPixId.newBuilder()
                    .setPixId(chaveExistente.pixId.toString())
                    .setClientId(chaveExistente.clientId.toString())
                    .build()
            ).build())

        // validação
        with(response) {
            assertEquals(chaveExistente.pixId.toString(), this.pixId)
            assertEquals(chaveExistente.clientId.toString(), this.clientId)
            assertEquals(chaveExistente.keyType.name, this.pixKey.keyType.name)
            assertEquals(chaveExistente.keyValue, this.pixKey.keyValue)
        }
    }

    @Test
    fun `should not load Pix key by pixId and clientId if filter invalid`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.get(GetKeyRequest.newBuilder()
                .setPixId(
                    FilterByPixId.newBuilder()
                    .setPixId("")
                    .setClientId("")
                    .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid arguments", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("pixId", "must not be blank"),
                Pair("clientId", "must not be blank"),
                Pair("pixId", "must be a UUID"),
                Pair("clientId", "must be a UUID"),
            ))
        }
    }

    @Test
    fun `should not load Pix key by pixId and clientId if key is not registered`() {
        // ação
        val notExistingPixKey = UUID.randomUUID().toString()
        val notExistingClientId = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.get(GetKeyRequest.newBuilder()
                .setPixId(FilterByPixId.newBuilder()
                    .setPixId(notExistingPixKey)
                    .setClientId(notExistingClientId)
                    .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix key not found", status.description)
        }
    }

    @Test
    fun `should load Pix key by keyValue if key is registered locally`() {
        // cenário
        val chaveExistente = repository.findByKeyValue("rafael.ponte@zup.com.br").get()

        // ação
        val response = grpcClient.get(GetKeyRequest.newBuilder()
            .setKey("rafael.ponte@zup.com.br")
            .build())

        // validação
        with(response) {
            assertEquals(chaveExistente.pixId.toString(), this.pixId)
            assertEquals(chaveExistente.clientId.toString(), this.clientId)
            assertEquals(chaveExistente.keyType.name, this.pixKey.keyType.name)
            assertEquals(chaveExistente.keyValue, this.pixKey.keyValue)
        }
    }

    @Test
    fun `should load Pix key by keyValue if key not exists locally but is registered in BCB`() {
        // cenário
        val bcbResponse = pixKeyDetailsResponse()
        `when`(bcbClient.findByKey(key = "user.from.another.bank@santander.com.br"))
        .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        // ação
        val response = grpcClient.get(GetKeyRequest.newBuilder()
            .setKey("user.from.another.bank@santander.com.br")
            .build())

        // validação
        with(response) {
            println(this)
            assertEquals("", this.pixId)
            assertEquals("", this.clientId)
            assertEquals(bcbResponse.keyType.name, this.pixKey.keyType.name)
            assertEquals(bcbResponse.key, this.pixKey.keyValue)
        }
    }

    @Test
    fun `should not load Pix key by keyValue if key not exists locally or in BCB`() {
        // cenário
        `when`(bcbClient.findByKey(key = "not.existing.user@santander.com.br"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.get(GetKeyRequest.newBuilder()
                .setKey("not.existing.user@santander.com.br")
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix key not found", status.description)
        }
    }

    @Test
    fun `should not load Pix key by keyValue if filter invalid`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.get(GetKeyRequest.newBuilder().setKey("").build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid arguments", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("keyValue", "must not be blank"),
            ))
        }
    }

    @Test
    fun `should not load Pix key if filter invalid`() {

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.get(GetKeyRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Pix key invalid or not entered", status.description)
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

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = KeyTypeBcb.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccountBcb {
        return BankAccountBcb(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = AccountTypeBcb.SVGS
        )
    }

    private fun owner(): OwnerBcb {
        return OwnerBcb(
            type = OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGetServiceGrpc.KeyManagerGetServiceBlockingStub? {
            return KeyManagerGetServiceGrpc.newBlockingStub(channel)
        }

    }

}