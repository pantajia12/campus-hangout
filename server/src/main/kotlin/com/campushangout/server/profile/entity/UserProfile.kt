package com.campushangout.server.profile.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_profiles")
class UserProfile(

    @Id
    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "nationality", length = 100)
    var nationality: String? = null,

    @Column(name = "major", length = 100)
    var major: String? = null,

    @Column(name = "bio", columnDefinition = "TEXT")
    var bio: String? = null,

    @Column(name = "color", nullable = false)
    var color: Short = 1
)
