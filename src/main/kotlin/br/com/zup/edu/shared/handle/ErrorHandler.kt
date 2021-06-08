package br.com.zup.edu.shared.handle

import io.micronaut.aop.Around

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPE,
)
@Retention(AnnotationRetention.RUNTIME)
@Around
annotation class ErrorHandler()