package game.ui;

import game.stage.evening.EveningGameLogic;

import javax.swing.*;
import javax.swing.SwingUtilities;

public class EveningGuiController {

    private final EveningGameLogic logic;
    private final EveningWindow window;

    // ✅ 저녁 스테이지 클리어(승리) 시 실행할 콜백(밤으로 이동)
    private final Runnable onClear;

    // 턴당 아이템 최대 2개
    private int itemsUsedThisTurn = 0;
    private static final int MAX_ITEMS_PER_TURN = 2;

    // 기존 호출 호환용
    public EveningGuiController() {
        this(null);
    }

    // ✅ 체인 연결용 생성자
    public EveningGuiController(Runnable onClear) {
        this.onClear = (onClear != null) ? onClear : () -> {};

        this.logic = new EveningGameLogic();
        this.window = new EveningWindow(this, logic);

        startPlayerTurn();
        window.appendLog("[SYSTEM] 저녁 스테이지 시작!");
        window.refreshAll();
    }

    // ---------------- 플레이어 턴 시작 ----------------
    private void startPlayerTurn() {
        itemsUsedThisTurn = 0;
        window.setButtonsEnabled(true);
    }

    // ---------------- 발사 버튼 ----------------
    public void onShootEnemy() {
        if (logic.isGameOver()) return;

        window.setButtonsEnabled(false);

        StringBuilder log = new StringBuilder();
        logic.shootEnemy(log);

        window.appendLog("[플레이어] " + log);
        window.refreshAll();

        if (logic.isGameOver()) {
            showResult();
            return;
        }

        maybeReloadThen(this::startDemonTurnSequence);
    }

    public void onShootSelf() {
        if (logic.isGameOver()) return;

        window.setButtonsEnabled(false);

        StringBuilder log = new StringBuilder();
        EveningGameLogic.TurnResult result = logic.shootSelf(log);

        window.appendLog("[플레이어] " + log);
        window.refreshAll();

        if (logic.isGameOver()) {
            showResult();
            return;
        }

        if (result == EveningGameLogic.TurnResult.TURN_CONTINUE) {
            // 내 턴 유지(아이템 사용 횟수도 유지)
            maybeReloadThen(() -> window.setButtonsEnabled(true));
        } else {
            // 턴 종료 -> 악마 턴
            maybeReloadThen(this::startDemonTurnSequence);
        }
    }

    // ---------------- 아이템 버튼 ----------------
    public void onUseProcrastinate() {
        useItemWithLimit((sb) -> logic.useProcrastinate(sb), false);
    }

    public void onUseRest() {
        useItemWithLimit((sb) -> logic.useRest(sb), false);
    }

    public void onUseGpt() {
        useItemWithLimit((sb) -> logic.useGpt(sb), false);
    }

    public void onUseRestart() {
        // 재장전는 "즉시 reload"는 로직이 처리, 연출(2초)만 컨트롤러에서
        useItemWithLimit((sb) -> logic.useRestart(sb), true);
    }

    private interface ItemAction {
        boolean run(StringBuilder sb);
    }

    private void useItemWithLimit(ItemAction action, boolean needsReloadAnim) {
        if (logic.isGameOver()) return;

        if (itemsUsedThisTurn >= MAX_ITEMS_PER_TURN) {
            window.appendLog("[SYSTEM] 이번 턴에는 아이템을 더 사용할 수 없다. (최대 " + MAX_ITEMS_PER_TURN + "개)");
            window.refreshAll();
            return;
        }

        StringBuilder sb = new StringBuilder();
        boolean used = action.run(sb);

        window.appendLog("[플레이어] " + sb);
        window.refreshAll();

        if (used) itemsUsedThisTurn++;

        if (used && needsReloadAnim) {
            // 재장전 연출(턴 유지)
            window.setButtonsEnabled(false);
            window.appendLog("[SYSTEM] 재장전 중입니다...");
            window.refreshAll();

            after(2000, () -> {
                window.appendLog("[SYSTEM] 장전 완료! (실탄 " + logic.getLiveCount() + " / 공포탄 " + logic.getBlankCount() + ")");
                window.refreshAll();
                window.setButtonsEnabled(true);
            });
        }
    }

    // ---------------- 악마 턴(고민2초 -> 겨누기2초 -> 발사) ----------------
    private void startDemonTurnSequence() {
        if (logic.isGameOver()) {
            showResult();
            return;
        }

        // 과제미루기: 악마 턴 스킵
        if (logic.consumeDemonSkip()) {
            window.appendLog("[과제 악마] ...이번 턴은 미뤄졌다. (턴 스킵)");
            window.refreshAll();
            startPlayerTurn();
            return;
        }

        // 1) 고민 2초
        window.appendLog("[과제 악마] 누구에게 총을 겨눌지 고민합니다...");
        window.refreshAll();

        after(2000, () -> {
            // 2) 겨누기 2초 (plan)
            EveningGameLogic.DemonTarget target = logic.planDemonTurn();
            if (target == EveningGameLogic.DemonTarget.PLAYER) {
                window.appendLog("[과제 악마] 당신에게 총구를 겨눴다...");
            } else {
                window.appendLog("[과제 악마] 자기 자신에게 총구를 겨눴다...");
            }
            window.refreshAll();

            after(2000, () -> {
                // 3) 발사
                StringBuilder log = new StringBuilder();
                EveningGameLogic.TurnResult result = logic.executePlannedDemonTurn(log);

                window.appendLog("[과제 악마] " + log);
                window.refreshAll();

                if (logic.isGameOver()) {
                    showResult();
                    window.setButtonsEnabled(false);
                    return;
                }

                if (result == EveningGameLogic.TurnResult.TURN_CONTINUE) {
                    // 악마 턴 유지 -> 다시 악마 턴
                    maybeReloadThen(this::startDemonTurnSequence);
                } else {
                    // 플레이어 턴 시작
                    maybeReloadThen(this::startPlayerTurn);
                }
            });
        });
    }

    // ---------------- 재장전 연출(2초) ----------------
    private void maybeReloadThen(Runnable next) {
        if (!logic.needsReload()) {
            next.run();
            return;
        }

        window.setButtonsEnabled(false);
        window.appendLog("[SYSTEM] 재장전 중입니다...");
        window.refreshAll();

        after(2000, () -> {
            logic.reload();
            window.appendLog("[SYSTEM] 장전 완료! (실탄 " + logic.getLiveCount() + " / 공포탄 " + logic.getBlankCount() + ")");
            window.refreshAll();
            next.run();
        });
    }

    // ---------------- Timer utils ----------------
    private void after(int ms, Runnable action) {
        Timer t = new Timer(ms, e -> action.run());
        t.setRepeats(false);
        t.start();
    }

    private void showResult() {
        String msg;
        boolean cleared = false;

        if (logic.getPlayerHp() <= 0 && logic.getDemonHp() <= 0) {
            msg = "무승부... 둘 다 쓰러졌다.";
        } else if (logic.getPlayerHp() <= 0) {
            msg = "패배... 과제 악마에게 지고 말았다.";
        } else {
            msg = "승리! 오늘의 과제를 처치했다!";
            cleared = true;
        }

        JOptionPane.showMessageDialog(window, msg);

        // ✅ 승리(클리어) 시 → 밤 스테이지로 이동
        if (cleared) {
            window.dispose();
            SwingUtilities.invokeLater(onClear);
        }
    }
}
