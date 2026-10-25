package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import ch.bbw.m450.tictactoe.players.HumanPlayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicTacToeTest {

    @ParameterizedTest
    @CsvSource({"CROSS,CIRCLE", "CIRCLE,CROSS"})
    void opponentReturnsOtherStone(Stone stone, Stone expected) {
        assertThat(stone.opponent()).isEqualTo(expected).isNotEqualTo(stone);
        assertThat(stone.opponent().opponent()).isEqualTo(stone);
    }

    @ParameterizedTest(name = "{index}: {1} wins on {0}")
    @MethodSource("winningLines")
    void detectsEveryWinningLineForBothColors(int[] positions, Stone stone) {
        var board = TicTacToeFixtures.boardWithWinningLine(positions, stone);
        assertThat(TicTacToeMain.isWin(board, stone)).as("winning color").isTrue();
        assertThat(TicTacToeMain.isWin(board, stone.opponent())).as("other color").isFalse();
    }

    static Stream<Arguments> winningLines() {
        return Stream.of(new int[]{0, 1, 2}, new int[]{3, 4, 5}, new int[]{6, 7, 8},
                        new int[]{0, 3, 6}, new int[]{1, 4, 7}, new int[]{2, 5, 8},
                        new int[]{0, 4, 8}, new int[]{2, 4, 6})
                .flatMap(line -> Arrays.stream(Stone.values()).map(stone -> Arguments.of(line, stone)));
    }

    @ParameterizedTest
    @EnumSource(Stone.class)
    void emptyAndDrawBoardsHaveNoWinner(Stone stone) {
        assertThat(TicTacToeMain.isWin(new Stone[9], stone)).as("empty board").isFalse();
        assertThat(TicTacToeMain.isWin(TicTacToeFixtures.FULL_BOARD_DRAW, stone)).as("draw").isFalse();
    }

    @ParameterizedTest
    @MethodSource("winningLines")
    void twoMatchingStonesAreNotAWin(int[] positions, Stone stone) {
        for (var missing : positions) {
            var board = TicTacToeFixtures.boardWithWinningLine(positions, stone);
            board[missing] = null;
            assertThat(TicTacToeMain.isWin(board, stone)).as("missing position %s", missing).isFalse();
            board[missing] = stone.opponent();
            assertThat(TicTacToeMain.isWin(board, stone)).as("mixed winning line").isFalse();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8})
    void greedyPlayerChoosesFirstFreeFieldWithoutChangingBoard(int firstFree) {
        var board = new Stone[9];
        Arrays.fill(board, 0, firstFree, Stone.CROSS);
        var before = board.clone();
        assertThat(new GreedyPlayer().play(board, Stone.CIRCLE)).isEqualTo(firstFree);
        assertThat(board).containsExactly(before);
    }

    @Test
    void greedyPlayerRejectsFullBoard() {
        var player = new GreedyPlayer();
        assertThatThrownBy(() -> player.play(TicTacToeFixtures.FULL_BOARD_DRAW, Stone.CROSS))
                .isInstanceOf(IllegalStateException.class).hasMessage("cannot play at all");
    }

    @Test
    void samePlayerCannotPlayBothSides() {
        var player = new GreedyPlayer();
        assertThatThrownBy(() -> TicTacToeMain.play(player, player))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("players must differ");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE, 9, 10, Integer.MAX_VALUE})
    @StdIo
    void rejectsInvalidPositions(int position, StdOut out) {
        var cross = new QueuePlayer(position);
        var circle = new GreedyPlayer();
        assertThatThrownBy(() -> TicTacToeMain.play(cross, circle))
                .isInstanceOf(IllegalStateException.class).hasMessage("cannot play to position " + position);
        assertThat(out.capturedString()).isEqualTo(TicTacToeMain.toString(new Stone[9]) + System.lineSeparator());
    }

    @Test
    @StdIo
    void rejectsOccupiedPosition(StdOut out) {
        var cross = new QueuePlayer(0);
        var circle = new QueuePlayer(0);
        assertThatThrownBy(() -> TicTacToeMain.play(cross, circle))
                .isInstanceOf(IllegalStateException.class).hasMessage("cannot play to position 0");
        assertThat(out.capturedString()).contains("\033[1mX\033[0m");
    }

    @ParameterizedTest
    @MethodSource("games")
    @StdIo
    void completeGamesReturnAndAnnounceExpectedResult(int[] crossMoves, int[] circleMoves, Stone expected, StdOut out) {
        var winner = TicTacToeMain.play(new QueuePlayer(crossMoves), new QueuePlayer(circleMoves));
        assertThat(winner).isEqualTo(expected);
        if (expected == null) {
            assertThat(out.capturedLines()).containsExactly("it's a draw!");
        } else {
            assertThat(out.capturedString()).endsWith("...and the winner is: " + expected + System.lineSeparator());
        }
    }

    static Stream<Arguments> games() {
        return Stream.of(
                Arguments.of(new int[]{0, 1, 2}, new int[]{3, 4}, Stone.CROSS),
                Arguments.of(new int[]{0, 1, 8}, new int[]{3, 4, 5}, Stone.CIRCLE),
                Arguments.of(new int[]{0, 2, 3, 7, 8}, new int[]{1, 4, 5, 6}, null),
                Arguments.of(new int[]{0, 2, 3, 7, 6}, new int[]{1, 4, 5, 8}, Stone.CROSS));
    }

    @Test
    @StdIo
    void playersReceiveAlternatingColorsAndDefensiveCopies(StdOut out) {
        var colors = new ArrayList<Stone>();
        TicTacToePlayer cross = (board, color) -> {
            colors.add(color);
            var free = IntStream.range(0, 9).filter(i -> board[i] == null).findFirst().orElseThrow();
            Arrays.fill(board, color.opponent());
            return free;
        };
        TicTacToePlayer circle = (board, color) -> {
            colors.add(color);
            return new GreedyPlayer().play(board, color);
        };
        assertThat(TicTacToeMain.play(cross, circle)).isEqualTo(Stone.CROSS);
        assertThat(colors).containsExactly(Stone.CROSS, Stone.CIRCLE, Stone.CROSS, Stone.CIRCLE,
                Stone.CROSS, Stone.CIRCLE, Stone.CROSS);
        assertThat(out.capturedString()).endsWith("...and the winner is: CROSS" + System.lineSeparator());
    }

    @Test
    void rendersExactBoardWithAnsiColorsAndRowBreaks() {
        var board = new Stone[]{Stone.CROSS, Stone.CIRCLE, null, null, null, null, null, null, null};
        assertThat(TicTacToeMain.toString(board)).isEqualTo("""
                \033[1mX\033[0m  \033[1mO\033[0m  \033[37m2\033[0m\s\s
                \033[37m3\033[0m  \033[37m4\033[0m  \033[37m5\033[0m\s\s
                \033[37m6\033[0m  \033[37m7\033[0m  \033[37m8\033[0m\s\s
                """);
    }

    @Test
    @StdIo({"0", "4", "8"})
    void humanReadsConsecutiveInputsAndPrintsPrompt(StdOut out) {
        var player = new HumanPlayer();
        var board = new Stone[9];
        assertThat(player.play(board, Stone.CROSS)).isZero();
        assertThat(player.play(board, Stone.CIRCLE)).isEqualTo(4);
        assertThat(player.play(board, Stone.CROSS)).isEqualTo(8);
        assertThat(out.capturedString()).isEqualTo(
                prompt(board, Stone.CROSS) + prompt(board, Stone.CIRCLE) + prompt(board, Stone.CROSS));
        assertThat(board).containsOnlyNulls();
    }

    private static String prompt(Stone[] board, Stone color) {
        return TicTacToeMain.toString(board) + "where to put the next " + color + "? (0-8): " + System.lineSeparator();
    }

    @Test
    @StdIo("abc")
    void humanRejectsNonNumericInput() {
        var player = new HumanPlayer();
        var board = new Stone[9];
        assertThatThrownBy(() -> player.play(board, Stone.CROSS))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @StdIo({"4", "8", "2", "6"})
    void mainRunsACompleteConsoleGame(StdOut out) {
        TicTacToeMain.main(new String[0]);
        assertThat(out.capturedString()).contains("where to put the next CROSS?")
                .endsWith("...and the winner is: CROSS" + System.lineSeparator());
    }
}
