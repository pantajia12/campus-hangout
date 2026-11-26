package com.campushangout.server.profile.entity

import jakarta.persistence.*

@Entity
@Table(name = "languages")
class Language(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "language_id")
    val languageId: Long? = null,

    @Column(name = "language_name", nullable = false, unique = true, length = 100)
    val languageName: String
)
