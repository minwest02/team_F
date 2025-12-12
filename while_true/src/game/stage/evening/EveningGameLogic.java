package game.stage.evening;

import java.util.Random;

/**
 * 저녁 스테이지 로직 (아이템 전 버전)
 * - 재장전은 자동이 아니라 needsReload()로 알림 -> 컨트롤러가 2초 연출 후 reload() 호출
 * - 자기에게 공포탄이면 턴 유지 (플레이어/악마 동일 적용)
 * - 악마는 planDemonTurn()으로 "겨눌 대상"과 "다음 탄(실/공포)"을 미리 결정 -> executePlannedDemonTurn()에서 실제 발사/소모
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

    // 탄창 (실탄 / 공포탄 개수)
    private int liveCount;
    private int blankCount;

    // 악마 턴 "미리 계획"
    private DemonTarget plannedTarget = null;
    private Boolean plannedBulletLive = null; // true=실탄, false=공포탄

    public EveningGameLogic() {
        // TODO: 나중에 아침/점심에서 넘어온 값으로 교체
        this(8, 5, 7, 6);
    }

    public EveningGameLogic(int health, int mental, int intelligence, int social) {
        this.health = clamp(health, 1, 10);
        this.mental = clamp(mental, 1, 10);
        this.intelligence = clamp(intelligence, 1, 10);
        this.social = clamp(social, 1, 10);

        this.playerHp = convertHealthToHp(this.health);

        reload(); // 첫 장전
    }

    // ---------- getters ----------
    public int getPlayerHp() { return playerHp; }
    public int getDemonHp() { return demonHp; }
    public int getLiveCount() { return liveCount; }
    public int getBlankCount() { return blankCount; }

    public int getHealth() { return health; }
    public int getMental() { return mental; }
    public int getIntelligence() { return intelligence; }
    public int getSocial() { return social; }

    public boolean isGameOver() {
        return playerHp <= 0 || demonHp <= 0;
    }

    /** live/blank 중 하나라도 0이면 재장전 필요 */
    public boolean needsReload() {
        return liveCount <= 0 || blankCount <= 0;
    }

    /** 컨트롤러가 연출 후 호출하는 장전: 6발 중 실탄 1~5 랜덤 */
    public void reload() {
        int total = 6;
        int live = 1 + random.nextInt(5); // 1..5
        int blank = total - live;

        liveCount = live;
        blankCount = blank;

        // 악마 계획은 장전하면 무효화
        plannedTarget = null;
        plannedBulletLive = null;
    }

    // ---------- 플레이어 ----------
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

    /** 자기에게 공포탄이면 턴 유지 */
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

    // ---------- 악마 ----------
    /** 악마가 누구를 겨눌지 + 다음 탄(실/공포)을 미리 결정 (겨누기 연출용) */
    public DemonTarget planDemonTurn() {
        if (isGameOver()) return null;

        plannedBulletLive = peekBullet();

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

    /** plan 결과로 실제 발사(탄 소모/피해 적용). 자기에게 공포탄이면 악마 턴 유지 */
    public TurnResult executePlannedDemonTurn(StringBuilder log) {
        if (isGameOver()) return TurnResult.TURN_END;

        if (plannedTarget == null || plannedBulletLive == null) {
            // 안전장치: 계획이 없으면 즉석 계획 후 실행
            planDemonTurn();
        }

        consumePlannedBullet();

        if (plannedTarget == DemonTarget.PLAYER) {
            log.append("당신을 노리고 방아쇠를 당겼다! ");
            if (plannedBulletLive) {
                playerHp = Math.max(0, playerHp - 1);
                log.append("💥 실탄! 내 HP가 1 줄었다.\n");
            } else {
                log.append("공포탄… 피해는 없었다.\n");
            }
            clearPlan();
            return TurnResult.TURN_END;
        }

        // 자기 자신
        log.append("자기 자신에게 방아쇠를 당겼다! ");
        if (plannedBulletLive) {
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

    // ---------- 탄 관련 ----------
    /** 소모 없이 다음 탄이 실탄인지 공포탄인지 '미리보기' */
    private boolean peekBullet() {
        int total = liveCount + blankCount;
        if (total <= 0) {
            // 컨트롤러가 보통 reload하지만 혹시 몰라 안전장치
            reload();
            total = liveCount + blankCount;
        }
        int r = random.nextInt(total);
        return r < liveCount;
    }

    /** 미리보기로 정해둔 탄을 실제로 소모 */
    private void consumePlannedBullet() {
        if (plannedBulletLive == null) return;

        if (plannedBulletLive) {
            if (liveCount > 0) liveCount--;
        } else {
            if (blankCount > 0) blankCount--;
        }
    }

    /** 실제 발사: 남은 탄에서 랜덤 1발 뽑아 소모 */
    private boolean drawBullet() {
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

    // ---------- util ----------
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
