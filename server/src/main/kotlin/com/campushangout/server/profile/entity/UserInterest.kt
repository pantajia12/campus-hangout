package com.campushangout.server.profile.entity

import jakarta.persistence.*

@Embeddable
data class UserInterestId(

    @Column(name = "user_id")
    val userId: Long = 0,

    @Column(name = "interest_id")
    val interestId: Long = 0
) : java.io.Serializable

@Entity
@Table(name = "user_interests")
class UserInterest(

    @EmbeddedId
    val id: UserInterestId
)
