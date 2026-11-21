package com.campushangout.server.account.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class StudentVerificationStatus {
    NOT_SUBMITTED,
    PENDING,
    APPROVED,
    REJECTED
}

@Entity
@Table(name = "student_verifications")
class StudentVerification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    val verificationId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: StudentVerificationStatus = StudentVerificationStatus.PENDING,

    @Column(name = "rejection_reason")
    var rejectionReason: String? = null,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null
)
