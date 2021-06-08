package br.com.zup.edu.pix.model

import br.com.zup.edu.AccountType
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.Valid

@Entity
class PixKey(
    @Column(nullable = false)
    val clientId: UUID,
    @Column(nullable = false, unique = true)
    val keyValue: String,
    @field:Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val keyType: PixKeyType,
    @field:Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val accountType: AccountType?,
    @field:Valid
    @Embedded
    val account: Account
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    val pixId: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
}