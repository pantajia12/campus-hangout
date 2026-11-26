package com.campushangout.server.profile.service

import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.common.error.BusinessException
import com.campushangout.server.common.error.ErrorCode
import com.campushangout.server.profile.dto.*
import com.campushangout.server.profile.entity.*
import com.campushangout.server.profile.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.JsonNode

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
            BusinessException(
                ErrorCode.NOT_FOUND,
                "사용자를 찾을 수 없습니다."
            )
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
    fun createProfile(
        userId: Long,
        request: CreateProfileRequest
    ): UserProfileResponse {

        if (!userRepository.existsById(userId)) {
            throw BusinessException(
                ErrorCode.NOT_FOUND,
                "사용자를 찾을 수 없습니다."
            )
        }

        if (userProfileRepository.existsById(userId)) {
            throw BusinessException(
                ErrorCode.CONFLICT,
                "이미 등록된 프로필입니다."
            )
        }

        val languageIds = request.languageIds.distinct()

        if (languageIds.isEmpty()) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT,
                "언어를 하나 이상 선택해주세요."
            )
        }

        if (
            languageRepository.findAllById(languageIds).size
            != languageIds.size
        ) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT,
                "존재하지 않는 언어가 포함되어 있습니다."
            )
        }

        userProfileRepository.save(
            UserProfile(
                userId = userId,
                name = request.name,
                nationality = request.nationality,
                major = request.major,
                bio = null,
                color = 1.toShort()
            )
        )

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

        return getMyProfile(userId)
    }

    @Transactional
    fun patchProfile(
        userId: Long,
        request: JsonNode
    ): UserProfileResponse {

        val user = userRepository.findById(userId).orElseThrow {
            BusinessException(
                ErrorCode.NOT_FOUND,
                "사용자를 찾을 수 없습니다."
            )
        }

        if (!user.verified) {
            throw BusinessException(
                ErrorCode.FORBIDDEN,
                "학생증 승인 후 이용할 수 있습니다."
            )
        }

        val profile = userProfileRepository.findById(userId).orElseThrow {
            BusinessException(
                ErrorCode.NOT_FOUND,
                "프로필을 찾을 수 없습니다."
            )
        }

        if (!request.isObject) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT,
                "요청 본문은 객체여야 합니다."
            )
        }

        val allowedFields = setOf(
            "bio",
            "color",
            "interestIds"
        )

        val fieldNames = request.properties()
            .map { it.key }
            .toSet()

        if (!allowedFields.containsAll(fieldNames)) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT,
                "수정할 수 없는 프로필 항목이 포함되어 있습니다."
            )
        }

        // 자기소개: 생략하면 유지, null이면 삭제
        if (request.has("bio")) {
            val bioNode = request.get("bio")

            if (!bioNode.isNull && !bioNode.isTextual) {
                throw BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "자기소개는 문자열 또는 null이어야 합니다."
                )
            }

            profile.bio = if (bioNode.isNull) {
                null
            } else {
                bioNode.asText()
            }
        }

        // 색상: 1~4
        if (request.has("color")) {
            val colorNode = request.get("color")

            if (
                !colorNode.isIntegralNumber ||
                !colorNode.canConvertToInt() ||
                colorNode.asInt() !in 1..4
            ) {
                throw BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "프로필 색상은 1부터 4까지 선택할 수 있습니다."
                )
            }

            profile.color = colorNode.asInt().toShort()
        }

        // 관심사: 생략하면 유지, []이면 전체 해제
        if (request.has("interestIds")) {
            val interestsNode = request.get("interestIds")

            if (!interestsNode.isArray) {
                throw BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "관심사 ID는 정수 배열이어야 합니다."
                )
            }

            val interestIds = mutableListOf<Long>()

            for (interestNode in interestsNode) {
                if (
                    !interestNode.isIntegralNumber ||
                    !interestNode.canConvertToLong()
                ) {
                    throw BusinessException(
                        ErrorCode.INVALID_INPUT,
                        "관심사 ID는 정수 배열이어야 합니다."
                    )
                }

                interestIds.add(interestNode.asLong())
            }

            val distinctInterestIds = interestIds.distinct()

            if (
                interestRepository.findAllById(distinctInterestIds).size
                != distinctInterestIds.size
            ) {
                throw BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "존재하지 않는 관심사가 포함되어 있습니다."
                )
            }

            userInterestRepository.deleteAllByIdUserId(userId)
            userInterestRepository.flush()

            userInterestRepository.saveAll(
                distinctInterestIds.map { interestId ->
                    UserInterest(
                        UserInterestId(
                            userId = userId,
                            interestId = interestId
                        )
                    )
                }
            )
        }

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
