package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.PerfectPlayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerfectPlayerTest {

    // Independent bit-board oracle: no calls to the production winner detection or negamax.
    private static final int[] WIN_MASKS = {7, 56, 448, 73, 146, 292, 273, 84};

    @Test
    void choosesAnOptimalMoveOnEveryReachableNonterminalBoard() {
        var visited = new HashSet<Integer>();
        var outcomes = new HashMap<Integer, Integer>();
        verifyAllBoards(0, 0, Stone.CROSS, visited, outcomes);
        assertThat(visited).as("all reachable boards where a move is still possible").hasSize(4520);
    }

    private static void verifyAllBoards(int cross, int circle, Stone turn, Set<Integer> visited,
                                        Map<Integer, Integer> outcomes) {
        if (wins(cross) || wins(circle) || (cross | circle) == 511) {
            return;
        }
        var key = cross | (circle << 9);
        if (!visited.add(key)) {
            return;
        }
        var board = board(cross, circle);
        var before = board.clone();
        var move = new PerfectPlayer().play(board, turn);
        assertThat(move).as("move on board %s", key).isBetween(0, 8);
        assertThat(before[move]).as("chosen field is empty").isNull();
        assertThat(board).as("input board remains unchanged").containsExactly(before);

        var nextCross = turn == Stone.CROSS ? cross | (1 << move) : cross;
        var nextCircle = turn == Stone.CIRCLE ? circle | (1 << move) : circle;
        assertThat(outcome(nextCross, nextCircle, turn.opponent(), outcomes))
                .as("optimal game-theoretic outcome on board %s", key)
                .isEqualTo(outcome(cross, circle, turn, outcomes));

        for (var position = 0; position < 9; position++) {
            var bit = 1 << position;
            if (((cross | circle) & bit) == 0) {
                verifyAllBoards(turn == Stone.CROSS ? cross | bit : cross,
                        turn == Stone.CIRCLE ? circle | bit : circle, turn.opponent(), visited, outcomes);
            }
        }
    }

    /** Cross maximizes and circle minimizes an outcome expressed from cross's perspective. */
    private static int outcome(int cross, int circle, Stone turn, Map<Integer, Integer> cache) {
        if (wins(cross)) {
            return 1;
        }
        if (wins(circle)) {
            return -1;
        }
        if ((cross | circle) == 511) {
            return 0;
        }
        var key = cross | (circle << 9);
        var cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        var best = turn == Stone.CROSS ? -2 : 2;
        for (var position = 0; position < 9; position++) {
            var bit = 1 << position;
            if (((cross | circle) & bit) == 0) {
                var result = outcome(turn == Stone.CROSS ? cross | bit : cross,
                        turn == Stone.CIRCLE ? circle | bit : circle, turn.opponent(), cache);
                best = turn == Stone.CROSS ? Math.max(best, result) : Math.min(best, result);
            }
        }
        cache.put(key, best);
        return best;
    }

    private static boolean wins(int stones) {
        for (var mask : WIN_MASKS) {
            if ((stones & mask) == mask) {
                return true;
            }
        }
        return false;
    }

    private static Stone[] board(int cross, int circle) {
        var board = new Stone[9];
        for (var position = 0; position < 9; position++) {
            var bit = 1 << position;
            if ((cross & bit) != 0) {
                board[position] = Stone.CROSS;
            } else if ((circle & bit) != 0) {
                board[position] = Stone.CIRCLE;
            }
        }
        return board;
    }

    @ParameterizedTest
    @EnumSource(Stone.class)
    void takesAnImmediateWinBeforeBlockingTheOpponent(Stone color) {
        var board = new Stone[]{color, color, null, color.opponent(), color.opponent(), null, null, null, null};
        assertThat(new PerfectPlayer().play(board, color)).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(Stone.class)
    void blocksAnImmediateLoss(Stone color) {
        var board = new Stone[]{color.opponent(), color.opponent(), null, color, null, null, null, null, null};
        assertThat(new PerfectPlayer().play(board, color)).isEqualTo(2);
    }

    @Test
    @StdIo
    void twoPerfectPlayersDraw(StdOut out) {
        assertThat(TicTacToeMain.play(new PerfectPlayer(), new PerfectPlayer())).isNull();
        assertThat(out.capturedLines()).containsExactly("it's a draw!");
    }

    @ParameterizedTest
    @EnumSource(Stone.class)
    void rejectsAlreadyWonBoards(Stone color) {
        var board = TicTacToeFixtures.boardWithWinningLine(new int[]{0, 1, 2}, color);
        assertThatThrownBy(() -> new PerfectPlayer().play(board, color.opponent()))
                .isInstanceOf(IllegalStateException.class).hasMessage("game is already over");
    }

    @Test
    void rejectsFullBoard() {
        assertThatThrownBy(() -> new PerfectPlayer().play(TicTacToeFixtures.FULL_BOARD_DRAW, Stone.CROSS))
                .isInstanceOf(IllegalStateException.class).hasMessage("cannot play at all");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 8, 10})
    void rejectsWrongBoardSize(int size) {
        assertThatThrownBy(() -> new PerfectPlayer().play(new Stone[size], Stone.CROSS))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("board must contain nine fields");
    }

    @Test
    void rejectsMissingBoardOrColor() {
        var player = new PerfectPlayer();
        assertThatThrownBy(() -> player.play(null, Stone.CROSS))
                .isInstanceOf(NullPointerException.class).hasMessage("board");
        assertThatThrownBy(() -> player.play(new Stone[9], null))
                .isInstanceOf(NullPointerException.class).hasMessage("colorToPlay");
    }
}
