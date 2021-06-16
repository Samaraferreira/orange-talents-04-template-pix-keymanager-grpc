package br.com.zup.edu.pix.search

import br.com.zup.edu.GetKeyRequest
import br.com.zup.edu.GetKeyResponse
import br.com.zup.edu.KeyManagerGetServiceGrpc
import br.com.zup.edu.client.bcb.BcbClient
import br.com.zup.edu.pix.model.PixKeyRepository
import br.com.zup.edu.shared.handle.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class SearchKeyEndpoint(
    @Inject private val repository: PixKeyRepository,
    @Inject private val bcbClient: BcbClient,
    @Inject private val validator: Validator
) : KeyManagerGetServiceGrpc.KeyManagerGetServiceImplBase() {

    override fun get(request: GetKeyRequest, responseObserver: StreamObserver<GetKeyResponse>) {
        val filter = request.toModel(validator)
        val keyInfo = filter.run(repository = repository, bcbClient = bcbClient)

        val response = GetPixKeyResponseConverter().convert(keyInfo)

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}