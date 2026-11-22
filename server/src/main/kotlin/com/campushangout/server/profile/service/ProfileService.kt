package com.campushangout.server.profile.service

import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.common.error.BusinessException
import com.campushangout.server.common.error.ErrorCode
import com.campushangout.server.profile.dto.*
import com.campushangout.server.profile.entity.*
import com.campushangout.server.profile.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val languageRepository: LanguageRepository,
    private val interestRepository: InterestRepository,
    private val userLanguageRepository: UserLanguageRepository,
    private val userInterestRepository: UserInterestRepository
) {

    @Transactional(readOnly = true)
    fun getMyProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findById(userId).orElseThrow {
            BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }

        val profile = userProfileRepository.findById(userId).orElse(null)

        return UserProfileResponse(
            userId = userId,
            email = user.email,
            verified = user.verified,
            profile = profile?.let {
                ProfileDetail(
                    name = it.name,
                    nationality = it.nationality,
                    major = it.major,
                    bio = it.bio,
                    color = it.color.toInt(),
                    languageIds = userLanguageRepository
                        .findAllByIdUserId(userId)
                        .map { relation -> relation.id.languageId }
                        .sorted(),
                    interestIds = userInterestRepository
                        .findAllByIdUserId(userId)
                        .map { relation -> relation.id.interestId }
                        .sorted()
                )
            }
        )
    }

    @Transactional
    fun updateProfile(
        userId: Long,
        request: UpdateProfileRequest
    ): UserProfileResponse {

        if (!userRepository.existsById(userId)) {
            throw BusinessException(
                ErrorCode.NOT_FOUND,
                "사용자를 찾을 수 없습니다."
            )
        }

        if (request.color !in 1..4) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT,
                "프로필 색상은 1부터 4까지 선택할 수 있습니다."
            )
        }

        val languageIds = request.languageIds.distinct()
        val interestIds = request.interestIds.distinct()

        if (languageRepository.findAllById(languageIds).size != languageIds.size) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT,
                "존재하지 않는 언어가 포함되어 있습니다."
            )
        }

        if (interestRepository.findAllById(interestIds).size != interestIds.size) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT,
                "존재하지 않는 관심사가 포함되어 있습니다."
            )
        }

        val profile = userProfileRepository.findById(userId).orElse(null)

        if (profile == null) {
            userProfileRepository.save(
                UserProfile(
                    userId = userId,
                    name = request.name,
                    nationality = request.nationality,
                    major = request.major,
                    bio = request.bio,
                    color = request.color.toShort()
                )
            )
        } else {
            profile.name = request.name
            profile.nationality = request.nationality
            profile.major = request.major
            profile.bio = request.bio
            profile.color = request.color.toShort()
        }

        // 기존 언어 연결을 새 선택 목록으로 교체
        userLanguageRepository.deleteAllByIdUserId(userId)
        userLanguageRepository.flush()

        userLanguageRepository.saveAll(
            languageIds.map { languageId ->
                UserLanguage(
                    UserLanguageId(
                        userId = userId,
                        languageId = languageId
                    )
                )
            }
        )

        // 기존 관심사 연결을 새 선택 목록으로 교체
        userInterestRepository.deleteAllByIdUserId(userId)
        userInterestRepository.flush()

        userInterestRepository.saveAll(
            interestIds.map { interestId ->
                UserInterest(
                    UserInterestId(
                        userId = userId,
                        interestId = interestId
                    )
                )
            }
        )

        return getMyProfile(userId)
    }

    @Transactional(readOnly = true)
    fun getLanguages(): List<OptionResponse> =
        languageRepository.findAll()
            .sortedBy { it.languageId }
            .map {
                OptionResponse(
                    id = requireNotNull(it.languageId),
                    name = it.languageName
                )
            }

    @Transactional(readOnly = true)
    fun getInterests(): List<OptionResponse> =
        interestRepository.findAll()
            .sortedBy { it.interestId }
            .map {
                OptionResponse(
                    id = requireNotNull(it.interestId),
                    name = it.interestName
                )
            }
}
