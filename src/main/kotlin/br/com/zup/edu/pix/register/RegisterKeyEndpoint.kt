package br.com.zup.edu.pix.register

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.CreateKeyResponse
import br.com.zup.edu.KeyManagerRegisterServiceGrpc
import br.com.zup.edu.shared.handle.ErrorHandler
import br.com.zup.edu.toModel
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegisterKeyEndpoint(val service: RegisterKeyService) : KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceImplBase() {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    override fun register(request: CreateKeyRequest?, responseObserver: StreamObserver<CreateKeyResponse>?) {
        LOGGER.info("New register key request: $request")

        val pixKey = service.register(request!!.toModel())

        responseObserver!!.onNext(CreateKeyResponse.newBuilder().setPixId(pixKey.pixId.toString()).build())
        responseObserver.onCompleted()
    }
}