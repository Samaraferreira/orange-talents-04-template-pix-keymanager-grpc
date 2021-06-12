package br.com.zup.edu.pix.model

import br.com.zup.edu.AccountType
import br.com.zup.edu.client.bcb.types.AccountTypeBcb

fun AccountType.toAccountTypeBcb(): AccountTypeBcb {
    return when(this) {
        AccountType.CONTA_CORRENTE -> AccountTypeBcb.CACC
        AccountType.CONTA_POUPANCA -> AccountTypeBcb.SVGS
        else -> throw IllegalArgumentException("AccountType invalid")
    }
}