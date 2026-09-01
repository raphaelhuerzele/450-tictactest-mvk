package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TicTacToeTest {

    @Test
    void junitDummyTest() {
        assertFalse(false);
    }

    @Test
    void assertJDummyTest() {
        assertThat(false).isFalse();
    }

    @Test
    void opponentShouldReturnTheOtherStone() {
        // GIVEN
        Stone cross = Stone.CROSS;
        Stone circle = Stone.CIRCLE;

        // WHEN
        Stone opponentOfCross = cross.opponent();
        Stone opponentOfCircle = circle.opponent();

        // THEN
        assertThat(opponentOfCross).isEqualTo(Stone.CIRCLE);
        assertThat(opponentOfCircle).isEqualTo(Stone.CROSS);
    }

    @Test
    void threeCrossesInTopRowShouldBeAWin() {
        // GIVEN
        Stone[] board = {
                Stone.CROSS, Stone.CROSS, Stone.CROSS,
                null, Stone.CIRCLE, null,
                Stone.CIRCLE, null, null
        };

        // WHEN
        boolean result = TicTacToeMain.isWin(board, Stone.CROSS);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    void threeCirclesOnDiagonalShouldBeAWin() {
        // GIVEN
        Stone[] board = {
                Stone.CIRCLE, Stone.CROSS, null,
                Stone.CROSS, Stone.CIRCLE, null,
                null, null, Stone.CIRCLE
        };

        // WHEN
        boolean result = TicTacToeMain.isWin(board, Stone.CIRCLE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    void boardWithoutThreeEqualStonesShouldNotBeAWin() {
        // GIVEN
        Stone[] board = {
                Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
                Stone.CIRCLE, Stone.CROSS, Stone.CIRCLE,
                Stone.CIRCLE, Stone.CROSS, null
        };

        // WHEN
        boolean crossWins = TicTacToeMain.isWin(board, Stone.CROSS);
        boolean circleWins = TicTacToeMain.isWin(board, Stone.CIRCLE);

        // THEN
        assertThat(crossWins).isFalse();
        assertThat(circleWins).isFalse();
    }

    @Test
    void greedyPlayerShouldChooseFirstEmptyField() {
        // GIVEN
        GreedyPlayer player = new GreedyPlayer();
        Stone[] board = {
                Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
                null, null, null,
                null, null, null
        };

        // WHEN
        int selectedField = player.play(board, Stone.CIRCLE);

        // THEN
        assertThat(selectedField).isEqualTo(3);
    }

    @Test
    void crossPlayerShouldWinACompleteGame() {
        // GIVEN
        int[] crossMoves = { 0, 1, 2 };
        int[] circleMoves = { 3, 4 };
        int[] crossIndex = { 0 };
        int[] circleIndex = { 0 };

        TicTacToePlayer crossPlayer = (board, color) -> crossMoves[crossIndex[0]++];
        TicTacToePlayer circlePlayer = (board, color) -> circleMoves[circleIndex[0]++];

        // WHEN
        Stone winner = TicTacToeMain.play(crossPlayer, circlePlayer);

        // THEN
        assertThat(winner).isEqualTo(Stone.CROSS);
    }
}