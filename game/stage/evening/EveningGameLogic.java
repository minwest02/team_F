package game.stage.evening;

import java.util.Random;

/**
 * EveningGameLogic (아이템 버전)
 * - 자동 재장전 없음: needsReload() -> 컨트롤러가 2초 연출 후 reload() 호출
 * - 자기 자신에게 공포탄이면 턴 유지 (플레이어/악마 동일)
 * - 악마 턴: planDemonTurn()로 겨눌 대상/탄 미리 결정 -> executePlannedDemonTurn()
 * - 아이템(플레이어만):
 *   1) 과제미루기: 악마 다음 턴 스킵
 *   2) 잠깐의휴식: HP +1(최대 5)
 *   3) GPT의도움: 다음 탄(실/공포) 확인(소모X, 다음 발사에 적용)
 *   4) 재장전: 즉시 장전(reload), 턴 유지(연출은 컨트롤러에서)
 */
public class EveningGameLogic {

    public enum DemonTarget { PLAYER, SELF }
    public enum TurnResult { TURN_END, TURN_CONTINUE }

    private final Random random = new Random();

    // 기본 스탯
    private int health;
    private int mental;
    private int intelligence;
    private int social;

    // 전투 스탯
    private int playerHp;   // 1~5
    private int demonHp = 5;

    // 탄창(남은 개수)
    private int liveCount;
    private int blankCount;

    // "다음 탄" 미리보기 캐시 (GPT / 악마 plan에서 사용)
    private Boolean cachedNextBulletLive = null;

    // 악마 plan
    private DemonTarget plannedTarget = null;
    private Boolean plannedBulletLive = null;

    // 아이템 카운트
    private int itemProcrastinate; // 과제미루기
    private int itemRest;          // 잠깐의휴식
    private int itemGpt;           // GPT의도움
    private int itemRestart;       // 재장전

    // 악마 턴 스킵 플래그
    private boolean demonSkipNext = false;

    public EveningGameLogic() {
        this(8, 5, 7, 6);
    }

    public EveningGameLogic(int health, int mental, int intelligence, int social) {
        this.health = clamp(health, 1, 10);
        this.mental = clamp(mental, 1, 10);
        this.intelligence = clamp(intelligence, 1, 10);
        this.social = clamp(social, 1, 10);

        this.playerHp = convertHealthToHp(this.health);

        reload();        // 첫 장전
        grantItems();    // 멘탈 기반 랜덤 지급
    }

    // ---------------- getters ----------------
    public int getPlayerHp() { return playerHp; }
    public int getDemonHp() { return demonHp; }
    public int getLiveCount() { return liveCount; }
    public int getBlankCount() { return blankCount; }

    public int getHealth() { return health; }
    public int getMental() { return mental; }
    public int getIntelligence() { return intelligence; }
    public int getSocial() { return social; }

    public int getItemProcrastinate() { return itemProcrastinate; }
    public int getItemRest() { return itemRest; }
    public int getItemGpt() { return itemGpt; }
    public int getItemRestart() { return itemRestart; }

    public boolean isGameOver() {
        return playerHp <= 0 || demonHp <= 0;
    }

    /** 둘 다 0발일 때만 재장전 필요 */
    public boolean needsReload() {
        return (liveCount + blankCount) <= 0;
    }

    /** 컨트롤러가 2초 연출 후 호출하는 실제 장전 */
    public void reload() {
        int total = 6;
        int live = 1 + random.nextInt(5); // 1..5
        int blank = total - live;

        liveCount = live;
        blankCount = blank;

        // 캐시/plan 초기화
        cachedNextBulletLive = null;
        plannedTarget = null;
        plannedBulletLive = null;
    }

    // ---------------- 아이템 지급 ----------------
    private void grantItems() {
        int give = mentalToItemCount(mental);
        for (int i = 0; i < give; i++) {
            int pick = random.nextInt(4);
            switch (pick) {
                case 0 -> itemProcrastinate++;
                case 1 -> itemRest++;
                case 2 -> itemGpt++;
                case 3 -> itemRestart++;
            }
        }
    }

    private int mentalToItemCount(int m) {
        if (m <= 2) return 0;
        if (m <= 4) return 1;
        if (m <= 6) return 2;
        if (m <= 8) return 3;
        return 4;
    }

    // ---------------- 플레이어: 발사 ----------------
    public TurnResult shootEnemy(StringBuilder log) {
        if (isGameOver()) return TurnResult.TURN_END;

        boolean bullet = drawBullet();
        if (bullet) {
            demonHp = Math.max(0, demonHp - 1);
            log.append("적에게 쐈다! 실탄이 맞아 과제 악마에게 1의 피해!\n");
        } else {
            log.append("적에게 쐈지만 공포탄이었다...\n");
        }
        return TurnResult.TURN_END;
    }

    /** 공포탄 + 자기에게 발사 -> TURN_CONTINUE */
    public TurnResult shootSelf(StringBuilder log) {
        if (isGameOver()) return TurnResult.TURN_END;

        boolean bullet = drawBullet();
        if (bullet) {
            playerHp = Math.max(0, playerHp - 1);
            log.append("자기 자신에게 쐈다... 실탄이다! 내 HP가 1 줄었다.\n");
            return TurnResult.TURN_END;
        } else {
            log.append("자기 자신에게 쐈다. 공포탄이다! 턴을 계속 유지한다.\n");
            return TurnResult.TURN_CONTINUE;
        }
    }

    // ---------------- 플레이어: 아이템 ----------------
    public boolean useProcrastinate(StringBuilder log) {
        if (itemProcrastinate <= 0) {
            log.append("과제미루기가 없다.\n");
            return false;
        }
        itemProcrastinate--;
        demonSkipNext = true;
        log.append("🗂️ [과제미루기] 과제 악마의 다음 턴이 스킵된다!\n");
        return true;
    }

    public boolean useRest(StringBuilder log) {
        if (itemRest <= 0) {
            log.append("잠깐의휴식이 없다.\n");
            return false;
        }
        itemRest--;
        int before = playerHp;
        playerHp = Math.min(5, playerHp + 1);
        log.append("🛌 [잠깐의휴식] HP 회복! (").append(before).append(" → ").append(playerHp).append(")\n");
        return true;
    }

    /** 다음 탄(실/공포) 확인. 소모 X, 다음 발사에 적용(캐시) */
    public boolean useGpt(StringBuilder log) {
        if (itemGpt <= 0) {
            log.append("GPT의도움이 없다.\n");
            return false;
        }
        itemGpt--;
        boolean next = peekNextBullet(); // 캐시 세팅
        log.append("🤖 [GPT의도움] 다음 탄은 ").append(next ? "실탄" : "공포탄").append("이다.\n");
        return true;
    }

    /** 즉시 재장전(턴 유지). 연출은 컨트롤러에서 2초 Timer로 처리 */
    public boolean useRestart(StringBuilder log) {
        if (itemRestart <= 0) {
            log.append("재장전이 없다.\n");
            return false;
        }
        itemRestart--;
        reload();
        log.append("🔄 [재장전] 즉시 재장전! (턴 유지)\n");
        return true;
    }

    // ---------------- 악마: 턴 스킵 ----------------
    public boolean consumeDemonSkip() {
        if (demonSkipNext) {
            demonSkipNext = false;
            return true;
        }
        return false;
    }

    // ---------------- 악마: plan / execute ----------------
    public DemonTarget planDemonTurn() {
        if (isGameOver()) return null;

        // ⭐ 한 종류만 남은 "확정 구간"이면 악마는 멍청하게 확률 굴리지 말고 최적 플레이
        if (blankCount == 0 && liveCount > 0) {          // 실탄만 남음
            plannedBulletLive = true;                    // 다음 탄은 실탄 확정
            plannedTarget = DemonTarget.PLAYER;          // 플레이어에게 쏴야 이득
            return plannedTarget;
        }
        if (liveCount == 0 && blankCount > 0) {          // 공포탄만 남음
            plannedBulletLive = false;                   // 다음 탄은 공포탄 확정
            plannedTarget = DemonTarget.SELF;            // 자기에게 쏴서 턴 유지 노림
            return plannedTarget;
        }

        plannedBulletLive = peekNextBullet(); // 캐시 세팅 + 탄 종류 확정

        int chanceShootPlayer;
        if (plannedBulletLive) {
            chanceShootPlayer = 80 - intelligence * 3;
            if (chanceShootPlayer < 30) chanceShootPlayer = 30;
        } else {
            chanceShootPlayer = 30 + intelligence * 3;
            if (chanceShootPlayer > 90) chanceShootPlayer = 90;
        }

        boolean targetPlayer = random.nextInt(100) < chanceShootPlayer;
        plannedTarget = targetPlayer ? DemonTarget.PLAYER : DemonTarget.SELF;
        return plannedTarget;
    }

    public TurnResult executePlannedDemonTurn(StringBuilder log) {
        if (isGameOver()) return TurnResult.TURN_END;

        if (plannedTarget == null || plannedBulletLive == null) {
            planDemonTurn();
        }

        // 계획한 "그 탄"을 실제로 소모(캐시를 쓰도록 drawBullet 호출)
        boolean bullet = drawBullet(); // 이게 plannedBulletLive와 동일해야 함

        if (plannedTarget == DemonTarget.PLAYER) {
            log.append("과제 악마가 당신을 노리고 방아쇠를 당겼다! ");
            if (bullet) {
                playerHp = Math.max(0, playerHp - 1);
                log.append("💥 실탄! 내 HP가 1 줄었다.\n");
            } else {
                log.append("공포탄… 피해는 없었다.\n");
            }
            clearPlan();
            return TurnResult.TURN_END;
        }

        // 자기 자신에게
        log.append("과제 악마가 자기 자신에게 방아쇠를 당겼다! ");
        if (bullet) {
            demonHp = Math.max(0, demonHp - 1);
            log.append("💥 실탄! 악마 HP가 1 줄었다.\n");
            clearPlan();
            return TurnResult.TURN_END;
        } else {
            log.append("공포탄이다! 악마는 다시 행동할 수 있다.\n");
            clearPlan();
            return TurnResult.TURN_CONTINUE;
        }
    }

    private void clearPlan() {
        plannedTarget = null;
        plannedBulletLive = null;
    }

    // ---------------- 탄 처리 ----------------
    /** 다음 탄 미리보기(소모X). 캐시에 저장해서 다음 drawBullet에 동일하게 반영 */
    private boolean peekNextBullet() {
        if (cachedNextBulletLive != null) return cachedNextBulletLive;

        int total = liveCount + blankCount;
        if (total <= 0) {
            // 안전장치(정상흐름이면 컨트롤러가 reload함)
            reload();
            total = liveCount + blankCount;
        }

        int r = random.nextInt(total);
        cachedNextBulletLive = (r < liveCount);
        return cachedNextBulletLive;
    }

    /** 실제 발사: 캐시가 있으면 그걸 먼저 소모 */
    private boolean drawBullet() {
        // 캐시 우선 소모(GPT/악마 plan이 봤던 "그 탄"을 그대로 쓴다)
        if (cachedNextBulletLive != null) {
            boolean b = cachedNextBulletLive;
            cachedNextBulletLive = null;
            if (b) liveCount--;
            else blankCount--;
            return b;
        }

        int total = liveCount + blankCount;
        if (total <= 0) {
            reload();
            total = liveCount + blankCount;
        }

        int r = random.nextInt(total);
        if (r < liveCount) {
            liveCount--;
            return true;
        } else {
            blankCount--;
            return false;
        }
    }

    // ---------------- 유틸 ----------------
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private int convertHealthToHp(int health) {
        int hp = (health + 1) / 2;
        if (hp < 1) hp = 1;
        if (hp > 5) hp = 5;
        return hp;
    }
}
