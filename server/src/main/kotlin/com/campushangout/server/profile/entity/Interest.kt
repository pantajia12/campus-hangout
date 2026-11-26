package com.campushangout.server.profile.entity

import jakarta.persistence.*

@Entity
@Table(name = "interests")
class Interest(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    val interestId: Long? = null,

    @Column(name = "interest_name", nullable = false, unique = true, length = 100)
    val interestName: String
)
