package fr.maxime.adapters.common

import fr.maxime.adapters.excel_adapter.technicals.HeaderColumn

data class UserToCreate(
    val firstName: String,
    val lastName: String,
    val divisions: Set<String>?,
    val username: String,
    val password: String,
    val email: String? = null,
    val row: Int,
)

const val firstNameUserHeader = "prénom"
const val lastNameUserHeader = "nom"
const val usernameUserHeader = "identifiant"
const val passwordUserHeader = "mot de passe"
const val divisionsUserHeader = "classes"
const val emailUserHeader = "email"
val userHeaders = listOf(
    HeaderColumn(firstNameUserHeader),
    HeaderColumn(lastNameUserHeader),
    HeaderColumn(usernameUserHeader),
    HeaderColumn(passwordUserHeader),
    HeaderColumn(divisionsUserHeader),
    HeaderColumn(emailUserHeader, optional = true)
)
