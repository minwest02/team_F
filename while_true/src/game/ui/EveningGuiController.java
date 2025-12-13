package game.ui;

import game.stage.evening.EveningGameLogic;

import javax.swing.*;

public class EveningGuiController {

    private final EveningGameLogic logic;
    private final EveningWindow window;

    public EveningGuiController() {
        this.logic = new EveningGameLogic();
        this.window = new EveningWindow(this, logic);
        setButtonsEnabled(true);
    }

    // --------- 플레이어 버튼 ---------

    public void onShootEnemy() {
        if (logic.isGameOver()) return;

        setButtonsEnabled(false);

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

        setButtonsEnabled(false);

        StringBuilder log = new StringBuilder();
        EveningGameLogic.TurnResult result = logic.shootSelf(log);

        window.appendLog("[플레이어] " + log);
        window.refreshAll();

        if (logic.isGameOver()) {
            showResult();
            return;
        }

        if (result == EveningGameLogic.TurnResult.TURN_CONTINUE) {
            // 내 턴 유지
            maybeReloadThen(() -> setButtonsEnabled(true));
        } else {
            // 악마 턴
            maybeReloadThen(this::startDemonTurnSequence);
        }
    }

    // --------- 악마 연출: 고민2초 -> 겨누기2초 -> 발사 ---------

    private void startDemonTurnSequence() {
        if (logic.isGameOver()) {
            showResult();
            return;
        }

        // 1) 고민 2초
        window.appendLog("[과제 악마] 누구에게 총을 겨눌지 고민합니다...");
        window.refreshAll();

        after(2000, () -> {
            // 2) 타겟 확정 + 겨누기 2초
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
                    setButtonsEnabled(false);
                    return;
                }

                if (result == EveningGameLogic.TurnResult.TURN_CONTINUE) {
                    // 악마 턴 유지 -> 다시 악마 연출
                    maybeReloadThen(this::startDemonTurnSequence);
                } else {
                    // 플레이어 턴
                    maybeReloadThen(() -> setButtonsEnabled(true));
                }
            });
        });
    }

    // --------- 재장전 연출 2초 ---------

    private void maybeReloadThen(Runnable next) {
        if (!logic.needsReload()) {
            next.run();
            return;
        }

        setButtonsEnabled(false);
        window.appendLog("[시스템] 재장전 중입니다...");
        window.refreshAll();

        after(2000, () -> {
            logic.reload();
            window.appendLog("[시스템] 장전 완료! (실탄 " + logic.getLiveCount() + " / 공포탄 " + logic.getBlankCount() + ")");
            window.refreshAll();
            next.run();
        });
    }

    // --------- utils ---------

    private void after(int ms, Runnable action) {
        Timer t = new Timer(ms, e -> action.run());
        t.setRepeats(false);
        t.start();
    }

    private void setButtonsEnabled(boolean enabled) {
        window.setButtonsEnabled(enabled);
    }

    private void showResult() {
        String msg;
        if (logic.getPlayerHp() <= 0 && logic.getDemonHp() <= 0) {
            msg = "무승부... 둘 다 쓰러졌다.";
        } else if (logic.getPlayerHp() <= 0) {
            msg = "패배... 과제 악마에게 지고 말았다.";
        } else {
            msg = "승리! 오늘의 과제를 처치했다!";
        }
        JOptionPane.showMessageDialog(window, msg);
    }
}
