package com.campushangout.server.profile.repository

import com.campushangout.server.profile.entity.Language
import org.springframework.data.jpa.repository.JpaRepository

interface LanguageRepository : JpaRepository<Language, Long>
