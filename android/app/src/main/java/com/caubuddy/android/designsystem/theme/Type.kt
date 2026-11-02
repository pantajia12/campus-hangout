package com.caubuddy.android.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

// 지금은 Material 3 기본 글꼴 크기를 쓰고 제목 굵기만 조정한다.
// 한글 가독성이 좋은 글꼴(예: Pretendard, OFL 라이선스) 적용은 Phase 7 디자인 다듬기에서 한다.
private val Base = Typography()

internal val CauBuddyTypography = Base.copy(
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
)
