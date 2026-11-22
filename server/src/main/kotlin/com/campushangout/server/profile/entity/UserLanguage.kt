package com.campushangout.server.profile.entity

import jakarta.persistence.*

@Embeddable
data class UserLanguageId(

    @Column(name = "user_id")
    val userId: Long = 0,

    @Column(name = "language_id")
    val languageId: Long = 0
) : java.io.Serializable

@Entity
@Table(name = "user_languages")
class UserLanguage(

    @EmbeddedId
    val id: UserLanguageId
)
