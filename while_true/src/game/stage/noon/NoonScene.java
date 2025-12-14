package game.stage.noon;

import game.core.Scene;

import java.util.Random;
import java.util.Scanner;

/**
 * NoonScene 클래스
 * - 점심 스테이지(미연시)를 담당하는 Scene 구현체.
 * - NPC와의 상호작용(호응/거절/무시)을 통해
 *   체력, 멘탈, 지식, 사교 상태값이 변하고,
 *   조건에 따라 Game Over가 발생함.
 */
public class NoonScene implements Scene {

    // ===== 상태값(플레이어 스탯) =====
    private int hp = 5;          // 체력
    private int mental = 5;      // 멘탈
    private int knowledge = 5;   // 지식
    private int social = 0;      // 사교

    // 상호작용 횟수
    private int interactionCount = 0;
    private static final int MAX_INTERACTIONS = 12; // 12~14회 중 기본값 12

    // 사교 최하(고립 루프) 여부 플래그
    private boolean inBlackZone = false;
    private static final int BLACK_ZONE_THRESHOLD = -5;

    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();

    @Override
    public void init() {
        // 장면 초기화: 시작 멘트 출력
        System.out.println("=== 점심 스테이지: 캠퍼스 미연시 시작 ===");
        System.out.println("주인공: 컴공과 2학년, 반복되는 하루 속에서 루프를 깨고 싶어 한다.\n");
    }

    @Override
    public void update() {
        // 점심 스테이지 전체를 이 메서드 안에서 진행
        runConversationLoop();
    }

    @Override
    public void render() {
        // 콘솔 버전에서는 별도 렌더링 없음 (update에서 바로 출력)
    }

    @Override
    public void handleInput() {
        // 콘솔 입력은 update() 내부에서 처리하므로 여기선 사용하지 않음.
    }

    /**
     * 점심 스테이지의 메인 루프
     * - NPC를 순서대로 등장시키고
     *   플레이어 선택에 따라 상태값을 변경함.
     */
    private void runConversationLoop() {
        while (interactionCount < MAX_INTERACTIONS) {
            interactionCount++;

            System.out.println("\n---------- [대화 " + interactionCount + "회차] ----------");

            // 1) 어떤 NPC가 나올지 결정 (우선 1~12 순서대로)
            int npcIndex = interactionCount;
            if (npcIndex > 12) {
                npcIndex = 12;
            }

            // 2) 해당 NPC 대사 + 선택지 출력
            showNpcDialogue(npcIndex);

            // 3) 플레이어 선택 입력 받기
            int choice = readChoice(); // 1: 호응, 2: 거절, 3: 무시

            // 4) 선택에 따른 상태 변화 적용
            applyNpcEffect(npcIndex, choice);

            // 5) 상태값 출력
            printStatus();

            // 6) Game Over 조건 체크
            if (checkGameOver()) {
                System.out.println(">>> 점심 스테이지에서 루프를 버티지 못했다...");
                return; // 점심 스테이지 종료
            }
        }

        System.out.println("\n=== 점심 스테이지 종료 ===");
        System.out.println("…였던 것 같은데.");
        System.out.println("왜 이렇게 익숙하지?");
    }

    /**
     * NPC의 기본 대사 + 루프 암시 멘트를 출력함.
     * - Black Zone에 들어가면 ??? / … 만 출력.
     */
    private void showNpcDialogue(int npc) {

        // 🔽 social이 임계값 이하로 내려가면 Black Zone 진입
        if (!inBlackZone && social <= BLACK_ZONE_THRESHOLD) {
            enterBlackZoneOnce();
        }

        // 🔽 Black Zone 상태면 누가 나와도 '...'만 보임
        if (inBlackZone) {
            System.out.println("??? : \"...\"");
            System.out.println("(주변이 이상할 정도로 조용하다. 아무도 말을 걸지 않는다.)");
            System.out.println("1) 대화에 응한다   2) 적당히 넘긴다   3) 무시한다");
            return;
        }

        // ===== 정상 상태일 때의 NPC 대사 =====
        switch (npc) {
            case 1: // 교수님
                System.out.println("교수님: \"과제 어디까지 진행됐습니까?\"");
                maybePrintLoopHint("아까도… 같은 표정이었지?");
                break;
            case 2: // 버스기사
                System.out.println("버스기사: \"오늘 학교 가?\"");
                maybePrintLoopHint("이 버스… 몇 번째 타는 거지?");
                break;
            case 3: // 학교 친구
                System.out.println("학교 친구: \"코딩 어디까지 했냐?\"");
                maybePrintLoopHint("너 계속… 이 부분에서 멈추는 것 같아.");
                break;
            case 4: // 선배
                System.out.println("선배: \"이 방향 맞는 거야?\"");
                maybePrintLoopHint("방금도… 여기서 서성이지 않았냐?");
                break;
            case 5: // 후배
                System.out.println("후배: \"선배님 이 코드 좀…\"");
                maybePrintLoopHint("오늘도, 같은 자리에서 기다리고 있었어요.");
                break;
            case 6: // 동아리 사람
                System.out.println("동아리 사람: \"오늘 연습 가능?\"");
                maybePrintLoopHint("이 말, 오늘만 몇 번째지…?");
                break;
            case 7: // 헬창
                System.out.println("헬창: \"하체 언제 할 거야?\"");
                maybePrintLoopHint("루틴만 반복하면, 시간 감각이 사라지더라.");
                break;
            case 8: // 식당 주인
                System.out.println("식당 주인: \"밥 먹고 가!\"");
                maybePrintLoopHint("오늘 메뉴도… 똑같이 시킬 거지?");
                break;
            case 9: // 대학원생
                System.out.println("대학원생: \"코딩은 사고의 문제입니다.\"");
                maybePrintLoopHint("방금 대답, 전에도 들은 것 같은데.");
                break;
            case 10: // 스님 (루프 인지자)
                System.out.println("스님: \"하루를 되풀이하는 중생이여…\"");
                System.out.println("스님: \"지금이 몇 번째 시도인지, 너는 모른다.\"");
                System.out.println("스님: \"하지만 나는… 전부 보고 있었다.\"");
                break;
            case 11: // 과대표
                System.out.println("과대표: \"공지 좀 읽어줘.\"");
                maybePrintLoopHint("왜 자꾸 같은 말만 하는 거지…?");
                break;
            case 12: // 조교
                System.out.println("조교: \"보고서 형식 다시 보세요.\"");
                maybePrintLoopHint("이 피드백, 계속 반복되는 것 같지 않아요?");
                break;
            default:
                System.out.println("??? : \"...\"");
        }

        System.out.println("1) 대화에 응한다   2) 적당히 넘긴다   3) 무시한다");
    }

    /**
     * 확률적으로 루프 암시 멘트를 출력하는 메서드
     * - 출력 형식: (힌트 내용)
     */
    private void maybePrintLoopHint(String hint) {
        if (random.nextDouble() < 0.3) { // 30% 확률
            System.out.println("   (" + hint + ")");
        }
    }

    /**
     * 플레이어에게 1/2/3 중 하나를 입력받음. (콘솔 테스트용)
     */
    private int readChoice() {
        while (true) {
            System.out.print("선택 (1:호응 / 2:거절 / 3:무시) > ");
            String line = scanner.nextLine().trim();

            if (line.equals("1")) return 1;  // 호응
            if (line.equals("2")) return 2;  // 거절
            if (line.equals("3")) return 3;  // 무시

            System.out.println("잘못된 입력입니다. 1, 2, 3 중 하나를 입력하세요.");
        }
    }

    /**
     * NPC + 선택(호응/거절/무시)에 따라 상태값을 변경하는 메서드.
     */
    private void applyNpcEffect(int npc, int choice) {

        // 🔽 Black Zone 상태에서는 어떤 선택을 해도
        //    "무시당한 느낌"만 나고 멘탈만 조금씩 깎임
        if (inBlackZone) {
            int dhp = 0;
            int dMental = -1;
            int dKnow = 0;
            int dSocial = 0;   // 이미 바닥이라 더 안 깎임

            hp        += dhp;
            mental    += dMental;
            knowledge += dKnow;
            social    += dSocial;

            System.out.println("\n[변화 로그] 체력 " + sign(dhp) +
                    " / 멘탈 " + sign(dMental) +
                    " / 지식 " + sign(dKnow) +
                    " / 사교 " + sign(dSocial));
            System.out.println("(입을 열려던 누군가가, 그냥 고개만 돌렸다.)");
            return;
        }

        // ===== 정상 상태에서의 NPC별 효과 =====
        int dhp = 0;
        int dMental = 0;
        int dKnow = 0;
        int dSocial = 0;

        switch (npc) {
            case 1: // 교수님
                if (choice == 1) {       // 호응
                    dKnow += 2; dMental -= 1;
                } else if (choice == 2) { // 거절
                    dMental -= 1; dSocial -= 1;
                } else {                // 무시
                    dKnow -= 1; dMental -= 2; dSocial -= 2;
                }
                break;

            case 2: // 버스기사
                if (choice == 1) {       // 호응
                    dSocial += 1;
                } else if (choice == 2) { // 거절
                    // 변화 없음
                } else {                // 무시
                    dSocial -= 1; dMental -= 1;
                }
                break;

            case 3: // 학교 친구
                if (choice == 1) {       // 호응
                    dSocial += 1; dMental -= 1;
                } else if (choice == 2) { // 거절
                    dMental += 1; dSocial -= 1;
                } else {                // 무시
                    dMental -= 1; dSocial -= 2;
                }
                break;

            case 4: // 선배
                if (choice == 1) {       // 호응
                    dKnow += 1; dMental -= 1;
                } else if (choice == 2) { // 거절
                    dMental += 1; dSocial -= 1;
                } else {                // 무시
                    dMental -= 2; dSocial -= 2;
                }
                break;

            case 5: // 후배
                if (choice == 1) {       // 호응
                    dSocial += 1; dMental -= 1;
                } else if (choice == 2) { // 거절
                    // 변화 없음
                } else {                // 무시
                    dSocial -= 2; dMental -= 1;
                }
                break;

            case 6: // 동아리 사람 (무시 포지티브)
                if (choice == 1) {       // 호응
                    dSocial += 1; dhp -= 1;
                } else if (choice == 2) { // 거절
                    dMental += 1; dSocial -= 1;
                } else {                // 무시
                    dMental += 1; dSocial -= 1;
                }
                break;

            case 7: // 헬창 (무시 포지티브)
                if (choice == 1) {       // 호응
                    dhp += 1;
                } else if (choice == 2) { // 거절
                    dSocial -= 1;
                } else {                // 무시
                    dMental += 1; dSocial -= 1;
                }
                break;

            case 8: // 식당 주인
                if (choice == 1) {       // 호응
                    dhp += 2; dSocial += 1;
                } else if (choice == 2) { // 거절
                    // 변화 없음
                } else {                // 무시
                    dhp -= 1; dSocial -= 1; dMental -= 1;
                }
                break;

            case 9: // 대학원생
                if (choice == 1) {       // 호응
                    dKnow += 2; dMental -= 2;
                } else if (choice == 2) { // 거절
                    dMental += 1;
                } else {                // 무시
                    dKnow -= 1; dMental -= 1; dSocial -= 2;
                }
                break;

            case 10: // 스님
                if (choice == 1) {       // 호응
                    dMental += 2; dKnow += 1;
                } else if (choice == 2) { // 거절
                    dMental += 1;
                } else {                // 무시
                    dMental += 1;        // 페널티 없음
                }
                break;

            case 11: // 과대표
                if (choice == 1) {       // 호응
                    dKnow += 1; dSocial += 1; dMental -= 1;
                } else if (choice == 2) { // 거절
                    dMental += 1; dSocial -= 1;
                } else {                // 무시
                    dMental += 1; dSocial -= 2;
                }
                break;

            case 12: // 조교
                if (choice == 1) {       // 호응
                    dKnow += 1; dMental -= 1;
                } else if (choice == 2) { // 거절
                    dSocial -= 1;
                } else {                // 무시
                    dKnow -= 1; dMental -= 1; dSocial -= 2;
                }
                break;
        }

        // 실제 상태값 반영
        hp        += dhp;
        mental    += dMental;
        knowledge += dKnow;
        social    += dSocial;

        System.out.println("\n[변화 로그] 체력 " + sign(dhp) +
                " / 멘탈 " + sign(dMental) +
                " / 지식 " + sign(dKnow) +
                " / 사교 " + sign(dSocial));
    }

    /** Black Zone 최초 진입 연출 */
    private void enterBlackZoneOnce() {
        inBlackZone = true;
        System.out.println("\n[Black Zone 진입]");
        System.out.println("사람들이… 너를 보지 않는다.");
        System.out.println("아니, 보지 않는 게 아니라— 이미 알고 있는 것 같다.");
        System.out.println("네가 어떻게 반응할지, 어디서 멈출지, 오늘이 몇 번째인지.");
    }

    /** 상태 변화값을 +n / -n / 0 형식으로 예쁘게 표시 */
    private String sign(int v) {
        if (v > 0) return "+" + v;
        if (v < 0) return String.valueOf(v);
        return "0";
    }

    /** 현재 상태값 출력 */
    private void printStatus() {
        System.out.println("현재 상태 → 체력: " + hp +
                " / 멘탈: " + mental +
                " / 지식: " + knowledge +
                " / 사교: " + social);
    }

    /** Game Over 조건 체크 (체력/멘탈/지식 0 이하) */
    private boolean checkGameOver() {
        if (hp <= 0) {
            System.out.println("Game Over - 체력 0 (기절 엔딩)");
            return true;
        }
        if (mental <= 0) {
            System.out.println("Game Over - 멘탈 0 (침잠 엔딩)");
            return true;
        }
        if (knowledge <= 0) {
            System.out.println("Game Over - 지식 0 (학사경고 엔딩)");
            return true;
        }
        return false;
    }
}
