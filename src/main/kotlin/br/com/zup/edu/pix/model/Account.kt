package br.com.zup.edu.pix.model

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
class Account(
    @field:NotBlank
    @field:Size(max = 6)
    @Column(nullable = false, length = 6)
    val accountNumber: String,

    @field:NotBlank
    @field:Size(max = 4)
    @Column(nullable = false, length = 4)
    val agencyNumber: String,

    @field:NotBlank
    @Column(nullable = false)
    val clientName: String,

    @field:Size(max = 11)
    @Column(nullable = false, length = 11)
    val clientCpf: String,

    @field:NotBlank
    @Column(nullable = false)
    val institutionName: String
)