package br.com.zup.edu.pix.remove

import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.DeleteKeyResponse
import br.com.zup.edu.KeyManagerDeleteServiceGrpc
import br.com.zup.edu.shared.handle.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveKeyEndpoint(val service: RemoveKeyService) : KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceImplBase() {

    override fun delete(request: DeleteKeyRequest?, responseObserver: StreamObserver<DeleteKeyResponse>?) {
        service.remove(request!!.clientId, request.pixId)

        responseObserver!!.onNext(DeleteKeyResponse.newBuilder().setPixId(request.pixId).build())
        responseObserver.onCompleted()
    }
}