package game.core;

/**
 * GameOverReason
 * - 게임오버 원인(스탯별)을 표현하는 enum
 *
 * [한줄 요약]
 * - UI가 "왜 게임오버인지"를 알 수 있게 해주는 표준 값임.
 */
public enum GameOverReason {

    NONE,           // 아직 게임오버 아님
    HP_ZERO,        // 체력 0
    MENTAL_ZERO,    // 멘탈 0
    KNOWLEDGE_ZERO, // 지식 0
    SOCIAL_MIN,     // (선택) 사교 최저치
    UNKNOWN         // 예외 / 미확정
}
