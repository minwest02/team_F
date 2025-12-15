package game.stage.noon;

import java.util.Random;

/**
 * NoonGameLogic
 * - 점심 스테이지(미연시) 진행을 담당하는 순수 로직 클래스.
 * - 콘솔 입출력 없음 / GUI에서 호출만 됨
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

    /** 게임 시작 시 처음 한 번 호출: 첫 NPC 대사 리턴 */
    public String start() {
        interactionCount = 0;
        gameOver = false;

        return "=== 점심 스테이지: 캠퍼스 미연시 시작 ===\n"
             + "주인공: 컴공과 2학년. 반복되는 하루 속에서 이미 루프를 자각하고 있다.\n"
             + "NPC들의 작은 힌트들을 통해, 오늘 루프에서 틈을 찾고자 한다.\n\n"
             + buildNpcDialogue(1);
    }

    /** 버튼 클릭 시 호출: 선택(1/2/3)을 반영하고 다음 대사를 리턴 */
    public String handleChoice(int choice) {
        if (gameOver) {
            return "[시스템] 이미 이 루프는 끝났습니다.";
        }

        // 이미 종료된 상태에서 또 선택 시
        if (interactionCount >= MAX_INTERACTIONS) {
            return "=== 점심 스테이지는 이미 종료되었다 ===\n"
                 + "NPC들의 반복되는 말들이 머리를 스친다.\n"
                 + "이 점심 루프에서는 더 바꿀 수 있는 게 없는 것 같다.\n"
                 + "다음 루프에서는… 다른 시간대에서 실마리를 찾아야 한다.";
        }

        int npcIndex = interactionCount + 1;

        // 선택 반영
        StringBuilder sb = new StringBuilder();
        sb.append("---------- [대화 ").append(npcIndex).append("회차 결과] ----------\n");

        // NPC + 선택에 따라 상태 변화 적용
        applyNpcEffect(npcIndex, choice, sb);

        // 상태 출력
        sb.append("\n현재 상태 → 체력: ").append(hp)
          .append(" / 멘탈: ").append(mental)
          .append(" / 지식: ").append(knowledge)
          .append(" / 사교: ").append(social)
          .append("\n");

        // Game Over 체크
        if (hp <= 0 || mental <= 0 || knowledge <= 0) {
            gameOver = true;

            if (hp <= 0) {
                sb.append("\nGame Over - 체력 0 (기절 엔딩)\n");
            } else if (mental <= 0) {
                sb.append("\nGame Over - 멘탈 0 (침잠 엔딩)\n");
            } else {
                sb.append("\nGame Over - 지식 0 (학사경고 엔딩)\n");
            }
            return sb.toString();
        }

        interactionCount++;

        // 다음 NPC가 남아 있으면 그 대사도 이어서 붙여줌
        if (interactionCount < MAX_INTERACTIONS) {
            int nextNpc = interactionCount + 1;

            sb.append("\n---------- [대화 ")
              .append(nextNpc).append("회차] ----------\n");

            sb.append(buildNpcDialogue(nextNpc));

        } else {
            // ===== 점심 루프 정상 종료 엔딩 =====
            sb.append("\n=== 점심 스테이지 종료 ===\n")
              .append("오늘 점심도… 결국 같은 흐름으로 흘러갔다.\n")
              .append("NPC들의 말 속에서 스쳐 지나간 ‘익숙함’과 ‘데자뷰’. \n")
              .append("주인공은 확신한다. 이건 단순한 기분 탓이 아니라…\n")
              .append("분명히 루프다.\n")
              .append("다음 루프에서는 다른 선택지를 찾아봐야 한다.");
        }

        return sb.toString();
    }

    /** 현재 NPC의 이름(로그용) */
    private String getNpcName(int npc) {
        return switch (npc) {
            case 1 -> "교수님";
            case 2 -> "버스기사";
            case 3 -> "학교 친구";
            case 4 -> "선배";
            case 5 -> "후배";
            case 6 -> "동아리 사람";
            case 7 -> "헬창";
            case 8 -> "식당 주인";
            case 9 -> "대학원생";
            case 10 -> "스님";
            case 11 -> "과대표";
            case 12 -> "조교";
            default -> "???";
        };
    }

    /** NPC 대사 + 루프 암시 + 선택지 문자열까지 만들어서 리턴 */
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
            default -> sb.append("??? : \"...\"\n");
        }

        sb.append("\n1) 대화에 응한다   2) 적당히 넘긴다   3) 무시한다\n");
        return sb.toString();
    }

    /** 30% 확률로 괄호 안에 루프 암시 멘트 추가 */
    private void maybeAppendHint(StringBuilder sb, String hint) {
        if (random.nextDouble() < 0.3) {
            sb.append("   (").append(hint).append(")\n");
        }
    }

    /** NPC + 선택(1/2/3)에 따른 상태 변화 적용, 변화 로그를 sb에 기록 */
    private void applyNpcEffect(int npc, int choice, StringBuilder sb) {
        int dhp = 0;
        int dMental = 0;
        int dKnow = 0;
        int dSocial = 0;

        switch (npc) {
            case 1 -> { // 교수님
                if (choice == 1)       { dKnow += 2; dMental -= 1; }
                else if (choice == 2) { dMental -= 1; dSocial -= 1; }
                else                  { dKnow -= 1; dMental -= 2; dSocial -= 2; }
            }
            case 2 -> { // 버스기사
                if (choice == 1)       { dSocial += 1; }
                else if (choice == 3) { dSocial -= 1; dMental -= 1; }
            }
            case 3 -> { // 학교 친구
                if (choice == 1)       { dSocial += 1; dMental -= 1; }
                else if (choice == 2) { dMental += 1; dSocial -= 1; }
                else                  { dMental -= 1; dSocial -= 2; }
            }
            case 4 -> { // 선배
                if (choice == 1)       { dKnow += 1; dMental -= 1; }
                else if (choice == 2) { dMental += 1; dSocial -= 1; }
                else                  { dMental -= 2; dSocial -= 2; }
            }
            case 5 -> { // 후배
                if (choice == 1)       { dSocial += 1; dMental -= 1; }
                else if (choice == 3) { dSocial -= 2; dMental -= 1; }
            }
            case 6 -> { // 동아리 사람
                if (choice == 1)       { dSocial += 1; dhp -= 1; }
                else                   { dMental += 1; dSocial -= 1; }
            }
            case 7 -> { // 헬창
                if (choice == 1)       { dhp += 1; }
                else if (choice == 2) { dSocial -= 1; }
                else                  { dMental += 1; dSocial -= 1; }
            }
            case 8 -> { // 식당 주인
                if (choice == 1)       { dhp += 2; dSocial += 1; }
                else if (choice == 3) { dhp -= 1; dSocial -= 1; dMental -= 1; }
            }
            case 9 -> { // 대학원생
                if (choice == 1)       { dKnow += 2; dMental -= 2; }
                else if (choice == 2) { dMental += 1; }
                else                  { dKnow -= 1; dMental -= 1; dSocial -= 2; }
            }
            case 10 -> { // 스님
                if (choice == 1)       { dMental += 2; dKnow += 1; }
                else                  { dMental += 1; }
            }
            case 11 -> { // 과대표
                if (choice == 1)       { dKnow += 1; dSocial += 1; dMental -= 1; }
                else if (choice == 2) { dMental += 1; dSocial -= 1; }
                else                  { dMental += 1; dSocial -= 2; }
            }
            case 12 -> { // 조교
                if (choice == 1)       { dKnow += 1; dMental -= 1; }
                else if (choice == 2) { dSocial -= 1; }
                else                  { dKnow -= 1; dMental -= 1; dSocial -= 2; }
            }
        }

        hp        += dhp;
        mental    += dMental;
        knowledge += dKnow;
        social    += dSocial;

        sb.append("[변화 로그] 체력 ").append(sign(dhp))
          .append(" / 멘탈 ").append(sign(dMental))
          .append(" / 지식 ").append(sign(dKnow))
          .append(" / 사교 ").append(sign(dSocial))
          .append("\n");
    }

    private String sign(int v) {
        if (v > 0) return "+" + v;
        if (v < 0) return String.valueOf(v);
        return "0";
    }

    /* ===============================
       ✅ [추가] 스테이지 전환용 상태 조회 메서드
       - GUI 컨트롤러가 "클리어/게임오버" 판단할 때 사용함
       =============================== */

    /** 현재 게임오버 상태인지 */
    public boolean isGameOver() {
        return gameOver;
    }

    /** 점심 스테이지를 정상적으로 끝까지 완료(클리어)했는지 */
    public boolean isCleared() {
        return !gameOver && interactionCount >= MAX_INTERACTIONS;
    }

    /** (선택) 진행도 확인이 필요하면 GUI에서 써도 됨 */
    public int getInteractionCount() {
        return interactionCount;
    }

    /** (선택) 총 대화 횟수(클리어 조건) */
    public int getMaxInteractions() {
        return MAX_INTERACTIONS;
    }
}
