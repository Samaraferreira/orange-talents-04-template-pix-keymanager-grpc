package br.com.zup.edu.shared.annotations

import br.com.zup.edu.pix.register.RegisterKeyRequest
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@MustBeDocumented
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "invalid PIX key",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)

@Singleton
class ValidPixKeyValidator: ConstraintValidator<ValidPixKey, RegisterKeyRequest> {
    override fun isValid(value: RegisterKeyRequest?, context: ConstraintValidatorContext?): Boolean {
        if (value?.keyValue == null) {
            return false
        }

        return value.keyType?.isValid(value.keyValue) ?: false
    }
}