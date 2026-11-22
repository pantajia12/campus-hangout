package com.campushangout.server.profile.repository

import com.campushangout.server.profile.entity.UserProfile
import org.springframework.data.jpa.repository.JpaRepository

interface UserProfileRepository : JpaRepository<UserProfile, Long>
