package br.com.zup.edu.client.bcb.types

data class BankAccountBcb(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeBcb
)