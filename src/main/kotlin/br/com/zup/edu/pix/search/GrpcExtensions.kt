package br.com.zup.edu.pix.search

import br.com.zup.edu.GetKeyRequest
import io.micronaut.validation.validator.Validator
import javax.validation.ConstraintViolationException
import br.com.zup.edu.GetKeyRequest.FilterCase.*

fun GetKeyRequest.toModel(validator: Validator): Filter {

    val filter = when(filterCase!!) {
        PIXID -> pixId.let {
            Filter.GetByPixId(clientId = it.clientId, pixId = it.pixId)
        }
        KEY -> Filter.GetByKey(key)
        FILTER_NOT_SET -> Filter.Invalid()
    }

    val violations = validator.validate(filter)

    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filter
}