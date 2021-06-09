package br.com.zup.edu.pix.remove

import br.com.zup.edu.shared.annotations.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
data class RemoveKeyRequest(
    @field:NotNull @ValidUUID("Client ID format is invalid") val clientId: UUID,
    @field:NotNull @ValidUUID("Pix ID format is invalid") val pixId: UUID
)