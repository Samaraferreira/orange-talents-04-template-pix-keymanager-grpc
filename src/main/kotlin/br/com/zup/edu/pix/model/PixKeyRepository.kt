package br.com.zup.edu.pix.model

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {
    fun existsByKeyValue(keyValue: String): Boolean

    fun existsByPixId(pixId: UUID): Boolean

    fun findByPixIdAndClientId(pixId: UUID, clientId: UUID): Optional<PixKey>

    fun findByKeyValue(key: String): Optional<PixKey>

    fun findAllByClientId(clientId: UUID): List<PixKey>
}