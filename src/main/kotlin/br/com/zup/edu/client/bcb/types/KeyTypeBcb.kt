package br.com.zup.edu.client.bcb.types

import br.com.zup.edu.pix.model.PixKeyType

enum class KeyTypeBcb {
    CPF, PHONE, EMAIL, RANDOM;

    fun toDomainType(): PixKeyType {
        return when(this) {
            CPF -> PixKeyType.CPF
            PHONE -> PixKeyType.PHONE_NUMBER
            EMAIL -> PixKeyType.EMAIL
            RANDOM -> PixKeyType.RANDOM_KEY
        }
    }
}
