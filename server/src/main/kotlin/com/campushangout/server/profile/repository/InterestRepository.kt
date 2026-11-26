package com.campushangout.server.profile.repository

import com.campushangout.server.profile.entity.Interest
import org.springframework.data.jpa.repository.JpaRepository

interface InterestRepository : JpaRepository<Interest, Long>
