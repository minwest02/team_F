package game.stage.noon;

import java.util.Random;

/**
 * NoonGameLogic
 * - 점심 스테이지(미연시) 진행을 담당하는 순수 로직 클래스
 * - UI / 콘솔과 완전히 분리된 게임 규칙 담당
 */
public class NoonGameLogic {

    // ===== 상태값(플레이어 스탯) =====
    private int hp = 5;          // 체력
    private int mental = 5;      // 멘탈
    private int knowledge = 5;   // 지식
    private int social = 0;      // 사교

    private int interactionCount = 0;
    private static final int MAX_INTERACTIONS = 12;

    private final Random random = new Random();
    private boolean gameOver = false;

    /* ===================== 외부 조회용 ===================== */

    /** 컨트롤러에서 GameOver 여부 확인용 */
    public boolean isGameOver() {
        return gameOver;
    }

    /* ===================== 시작 ===================== */

    /** 게임 시작 시 처음 한 번 호출 */
    public String start() {
        interactionCount = 0;
        gameOver = false;

        hp = 5;
        mental = 5;
        knowledge = 5;
        social = 0;

        return "=== 점심 스테이지: 캠퍼스 미연시 시작 ===\n"
             + "주인공: 컴공과 2학년. 반복되는 하루 속에서 이미 루프를 자각하고 있다.\n"
             + "NPC들의 작은 힌트들을 통해, 오늘 루프에서 틈을 찾고자 한다.\n\n"
             + buildNpcDialogue(1);
    }

    /* ===================== 선택 처리 ===================== */

    /** 버튼 클릭 시 호출 */
    public String handleChoice(int choice) {
        if (gameOver) {
            return "[시스템] 이미 이 루프는 끝났습니다.";
        }

        if (interactionCount >= MAX_INTERACTIONS) {
            return "=== 점심 스테이지는 이미 종료되었다 ===\n"
                 + "NPC들의 반복되는 말들이 머리를 스친다.\n"
                 + "다음 루프에서는… 다른 시간대에서 실마리를 찾아야 한다.";
        }

        int npcIndex = interactionCount + 1;

        StringBuilder sb = new StringBuilder();
        sb.append("---------- [대화 ").append(npcIndex).append("회차 결과] ----------\n");

        // 1️⃣ 선택에 따른 상태 변화
        applyNpcEffect(npcIndex, choice, sb);

        // 2️⃣ 상태값 범위 제한
        clampStats();

        // 3️⃣ 상태 출력
        sb.append("\n현재 상태 → 체력: ").append(hp)
          .append(" / 멘탈: ").append(mental)
          .append(" / 지식: ").append(knowledge)
          .append(" / 사교: ").append(social)
          .append("\n");

        // 4️⃣ Game Over 판정
        if (checkGameOver(sb)) {
            return sb.toString();
        }

        interactionCount++;

        // 5️⃣ 다음 NPC 대사
        if (interactionCount < MAX_INTERACTIONS) {
            int nextNpc = interactionCount + 1;

            sb.append("\n---------- [대화 ")
              .append(nextNpc).append("회차] ----------\n");

            sb.append(buildNpcDialogue(nextNpc));
        } else {
            sb.append("\n=== 점심 스테이지 종료 ===\n")
              .append("오늘 점심도… 결국 같은 흐름으로 흘러갔다.\n")
              .append("이건 단순한 기분 탓이 아니라… 분명히 루프다.");
        }

        return sb.toString();
    }

    /* ===================== Game Over ===================== */

    /** Game Over 판정 + 엔딩 문구 */
    private boolean checkGameOver(StringBuilder sb) {
        if (hp <= 0 || mental <= 0 || knowledge <= 0) {
            gameOver = true;

            if (hp <= 0) {
                sb.append("\nGame Over - 체력 0 (기절 엔딩)\n");
            } else if (mental <= 0) {
                sb.append("\nGame Over - 멘탈 0 (침잠 엔딩)\n");
            } else {
                sb.append("\nGame Over - 지식 0 (학사경고 엔딩)\n");
            }
            return true;
        }
        return false;
    }

    /* ===================== NPC 대사 ===================== */

    private String buildNpcDialogue(int npc) {
        StringBuilder sb = new StringBuilder();

        switch (npc) {
            case 1 -> {
                sb.append("교수님: \"과제 어디까지 진행됐습니까?\"\n");
                maybeAppendHint(sb, "표정이… 아까와 똑같이 느껴진다.");
            }
            case 2 -> {
                sb.append("버스기사: \"오늘 학교 가?\"\n");
                maybeAppendHint(sb, "이 버스, 몇 번째 타고 있는 거지…?");
            }
            case 3 -> {
                sb.append("학교 친구: \"코딩 어디까지 했냐?\"\n");
                maybeAppendHint(sb, "너… 계속 같은 질문을 하는 것 같은데?");
            }
            case 4 -> {
                sb.append("선배: \"이 방향 맞는 거야?\"\n");
                maybeAppendHint(sb, "아까도 여기서 길을 물어본 것 같았다.");
            }
            case 5 -> {
                sb.append("후배: \"선배님 이 코드 좀…\"\n");
                maybeAppendHint(sb, "오늘도 같은 자리에서 기다리고 있었네.");
            }
            case 6 -> {
                sb.append("동아리 사람: \"오늘 연습 가능?\"\n");
                maybeAppendHint(sb, "이 말, 오늘만 몇 번째지…?");
            }
            case 7 -> {
                sb.append("헬창: \"하체 언제 할 거야?\"\n");
                maybeAppendHint(sb, "루틴만 반복하면… 시간 감각이 흐려지더라.");
            }
            case 8 -> {
                sb.append("식당 주인: \"밥 먹고 가!\"\n");
                maybeAppendHint(sb, "오늘 메뉴도… 똑같이 시키려나?");
            }
            case 9 -> {
                sb.append("대학원생: \"코딩은 사고의 문제입니다.\"\n");
                maybeAppendHint(sb, "방금 그 말… 전에 들은 적 있었는데.");
            }
            case 10 -> {
                sb.append("스님: \"하루를 되풀이하는 중생이여…\"\n");
                sb.append("스님: \"너는 지금 몇 번째 시도인지 알고 있는가?\"\n");
                sb.append("스님: \"나는… 전부 지켜보고 있다.\"\n");
            }
            case 11 -> {
                sb.append("과대표: \"공지 좀 읽어줘.\"\n");
                maybeAppendHint(sb, "왜 자꾸 같은 말만 반복하는 걸까…?");
            }
            case 12 -> {
                sb.append("조교: \"보고서 형식 다시 보세요.\"\n");
                maybeAppendHint(sb, "이 피드백… 계속 반복되는 느낌이다.");
            }
        }

        sb.append("\n1) 대화에 응한다   2) 적당히 넘긴다   3) 무시한다\n");
        return sb.toString();
    }

    /* ===================== 루프 힌트 ===================== */

    /** 회차가 쌓일수록 힌트 확률 증가 */
    private void maybeAppendHint(StringBuilder sb, String hint) {
        double base = 0.2;
        double bonus = interactionCount * 0.05;
        if (random.nextDouble() < Math.min(0.6, base + bonus)) {
            sb.append("   (").append(hint).append(")\n");
        }
    }

    /* ===================== 상태 변화 ===================== */

    private void applyNpcEffect(int npc, int choice, StringBuilder sb) {
        int dhp = 0, dMental = 0, dKnow = 0, dSocial = 0;

        switch (npc) {
            case 1 -> {
                if (choice == 1)       { dKnow += 2; dMental -= 1; }
                else if (choice == 2) { dMental -= 1; dSocial -= 1; }
                else                  { dKnow -= 1; dMental -= 2; dSocial -= 2; }
            }
            case 2 -> {
                if (choice == 1)       { dSocial += 1; }
                else if (choice == 3) { dSocial -= 1; dMental -= 1; }
            }
            case 3 -> {
                if (choice == 1)       { dSocial += 1; dMental -= 1; }
                else if (choice == 2) { dMental += 1; dSocial -= 1; }
                else                  { dMental -= 1; dSocial -= 2; }
            }
            case 4 -> {
                if (choice == 1)       { dKnow += 1; dMental -= 1; }
                else if (choice == 2) { dMental += 1; dSocial -= 1; }
                else                  { dMental -= 2; dSocial -= 2; }
            }
            case 5 -> {
                if (choice == 1)       { dSocial += 1; dMental -= 1; }
                else if (choice == 3) { dSocial -= 2; dMental -= 1; }
            }
            case 6 -> {
                if (choice == 1)       { dSocial += 1; dhp -= 1; }
                else                   { dMental += 1; dSocial -= 1; }
            }
            case 7 -> {
                if (choice == 1)       { dhp += 1; }
                else if (choice == 2) { dSocial -= 1; }
                else                  { dMental += 1; dSocial -= 1; }
            }
            case 8 -> {
                if (choice == 1)       { dhp += 2; dSocial += 1; }
                else if (choice == 3) { dhp -= 1; dSocial -= 1; dMental -= 1; }
            }
            case 9 -> {
                if (choice == 1)       { dKnow += 2; dMental -= 2; }
                else if (choice == 2) { dMental += 1; }
                else                  { dKnow -= 1; dMental -= 1; dSocial -= 2; }
            }
            case 10 -> {
                if (choice == 1)       { dMental += 2; dKnow += 1; }
                else                  { dMental += 1; }
            }
            case 11 -> {
                if (choice == 1)       { dKnow += 1; dSocial += 1; dMental -= 1; }
                else if (choice == 2) { dMental += 1; dSocial -= 1; }
                else                  { dMental += 1; dSocial -= 2; }
            }
            case 12 -> {
                if (choice == 1)       { dKnow += 1; dMental -= 1; }
                else if (choice == 2) { dSocial -= 1; }
                else                  { dKnow -= 1; dMental -= 1; dSocial -= 2; }
            }
        }

        hp += dhp;
        mental += dMental;
        knowledge += dKnow;
        social += dSocial;

        sb.append("[변화 로그] 체력 ").append(sign(dhp))
          .append(" / 멘탈 ").append(sign(dMental))
          .append(" / 지식 ").append(sign(dKnow))
          .append(" / 사교 ").append(sign(dSocial))
          .append("\n");
    }

    /* ===================== 유틸 ===================== */

    private void clampStats() {
        hp = clamp(hp, 0, 10);
        mental = clamp(mental, 0, 10);
        knowledge = clamp(knowledge, 0, 10);
        social = clamp(social, -10, 10);
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private String sign(int v) {
        if (v > 0) return "+" + v;
        if (v < 0) return String.valueOf(v);
        return "0";
    }
}
