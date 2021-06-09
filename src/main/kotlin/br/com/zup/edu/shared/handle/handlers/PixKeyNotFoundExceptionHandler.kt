package br.com.zup.edu.shared.handle.handlers

import br.com.zup.edu.shared.exceptions.PixKeyNotFoundException
import br.com.zup.edu.shared.handle.ExceptionHandler
import br.com.zup.edu.shared.handle.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixKeyNotFoundExceptionHandler : ExceptionHandler<PixKeyNotFoundException> {
    override fun handle(e: PixKeyNotFoundException): StatusWithDetails {
        return StatusWithDetails(Status.NOT_FOUND.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is PixKeyNotFoundException
    }
}