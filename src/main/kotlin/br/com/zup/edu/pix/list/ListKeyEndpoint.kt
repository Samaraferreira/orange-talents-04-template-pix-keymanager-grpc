package br.com.zup.edu.pix.list

import br.com.zup.edu.KeyManagerGetAllServiceGrpc
import br.com.zup.edu.KeyType
import br.com.zup.edu.ListAllKeysRequest
import br.com.zup.edu.ListAllKeysResponse
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.shared.handle.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.UUID
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListKeysEndpoint(
    val repository: PixKeyRepository
) : KeyManagerGetAllServiceGrpc.KeyManagerGetAllServiceImplBase() {

    override fun getAll(request: ListAllKeysRequest, responseObserver: StreamObserver<ListAllKeysResponse>) {

        if (request.clientId.isNullOrBlank())
            throw IllegalArgumentException("ClientId must not be blank or null")

        val clientId = UUID.fromString(request.clientId)

        val listOfResponse = repository.findAllByClientId(clientId).map { key ->
            ListAllKeysResponse.PixKey.newBuilder()
                .setPixId(key.pixId.toString())
                .setAccountType(key.account.accountType)
                .setKeyType(KeyType.valueOf(key.keyType.name))
                .setKeyValue(key.keyValue)
                .setCreatedAt(key.createdAt.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                }).build()
        }

        responseObserver.onNext(ListAllKeysResponse.newBuilder()
                                    .setClientId(clientId.toString())
                                    .addAllKeys(listOfResponse)
                                    .build())
        responseObserver.onCompleted()
    }
}