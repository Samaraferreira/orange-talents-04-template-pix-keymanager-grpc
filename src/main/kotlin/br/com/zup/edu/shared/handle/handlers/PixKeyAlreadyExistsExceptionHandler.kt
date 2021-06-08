package br.com.zup.edu.shared.handle.handlers

import br.com.zup.edu.shared.exceptions.PixKeyAlreadyExistsException
import br.com.zup.edu.shared.handle.ExceptionHandler
import br.com.zup.edu.shared.handle.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixKeyAlreadyExistsExceptionHandler : ExceptionHandler<PixKeyAlreadyExistsException> {
    override fun handle(e: PixKeyAlreadyExistsException): StatusWithDetails {
        return StatusWithDetails(Status.ALREADY_EXISTS.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is PixKeyAlreadyExistsException
    }
}