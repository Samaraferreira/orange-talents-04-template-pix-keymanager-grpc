package br.com.zup.edu.shared.annotations

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@ReportAsSingleViolation
@MustBeDocumented
@Target(
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME)
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
    flags = [Pattern.Flag.CASE_INSENSITIVE],
    message = "The value must be a UUID")
@Constraint(validatedBy = [])
annotation class ValidUUID (
    val message: String = "The value must be a UUID",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)