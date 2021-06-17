package br.com.zup.edu.pix.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PixKeyTypeTest {

    @Nested // aninha vários testes
    inner class RANDOM_KEY {
        @Test
        fun `should be valid if key value is null or blank`() {
            with(PixKeyType.RANDOM_KEY) {
                assertTrue(isValid(null))
                assertTrue(isValid(""))
            }
        }

        @Test
        fun `should not be valid if key value is not null or blank`() {
            with(PixKeyType.RANDOM_KEY) {
                assertFalse(isValid("uma chave qualquer"))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `should be valid if email is valid`() {
            with(PixKeyType.EMAIL) {
                assertTrue(isValid("test@teste.com"))
            }
        }

        @Test
        fun `should not be valid if email is null or blank`() {
            with(PixKeyType.EMAIL) {
                assertFalse(isValid(null))
                assertFalse(isValid(""))
            }
        }

        @Test
        fun `should not be valid if email is invalid`() {
            with(PixKeyType.EMAIL) {
                assertFalse(isValid("test"))
            }
        }
    }

    @Nested
    inner class CPF {

        @Test
        fun `should be valid if cpf is valid`() {
            with(PixKeyType.CPF) {
                assertTrue(isValid("26332985089"))
            }
        }

        @Test
        fun `should not be valid if cpf is null or blank`() {
            with(PixKeyType.CPF) {
                assertFalse(isValid(null))
                assertFalse(isValid(""))
            }
        }

        @Test
        fun `should not be valid if cpf is invalid`() {
            with(PixKeyType.CPF) {
                assertFalse(isValid("315.251.220-10"))
            }
        }
    }

    @Nested
    inner class PHONE_NUMBER {

        @Test
        fun `should be valid if phone number is valid`() {
            with(PixKeyType.PHONE_NUMBER) {
                assertTrue(isValid("+5585988714077"))
            }
        }

        @Test
        fun `should not be valid if phone number is null or blank`() {
            with(PixKeyType.PHONE_NUMBER) {
                assertFalse(isValid(null))
                assertFalse(isValid(""))
            }
        }

        @Test
        fun `should not be valid if phone number is invalid`() {
            with(PixKeyType.PHONE_NUMBER) {
                assertFalse(isValid("5585988714077"))
            }
        }

    }
}