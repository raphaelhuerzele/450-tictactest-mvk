package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import ch.bbw.m450.tictactoe.players.HumanPlayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicTacToeTest {

    @ParameterizedTest(name = "{0}.opponent() == {1}")
    @CsvSource({
            "CROSS,CIRCLE",
            "CIRCLE,CROSS"
    })
    void opponentReturnsOtherStone(Stone stone, Stone expected) {
        assertThat(stone.opponent()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{index}: {1} wins")
    @MethodSource("winningLines")
    void isWinDetectsAllWinningLines(int[] positions, Stone stone) {
        Stone[] board = TicTacToeFixtures.boardWithWinningLine(positions, stone);

        assertThat(TicTacToeMain.isWin(board, stone)).isTrue();
        assertThat(TicTacToeMain.isWin(board, stone.opponent())).isFalse();
    }

    static Stream<Arguments> winningLines() {
        return Stream.of(
                Arguments.of(new int[]{0, 1, 2}, Stone.CROSS),
                Arguments.of(new int[]{3, 4, 5}, Stone.CIRCLE),
                Arguments.of(new int[]{6, 7, 8}, Stone.CROSS),
                Arguments.of(new int[]{0, 3, 6}, Stone.CIRCLE),
                Arguments.of(new int[]{1, 4, 7}, Stone.CROSS),
                Arguments.of(new int[]{2, 5, 8}, Stone.CIRCLE),
                Arguments.of(new int[]{0, 4, 8}, Stone.CROSS),
                Arguments.of(new int[]{2, 4, 6}, Stone.CIRCLE)
        );
    }

    @Test
    void namedRowFixtureIsAWin() {
        assertThat(TicTacToeMain.isWin(TicTacToeFixtures.ROW_WIN_CROSS, Stone.CROSS)).isTrue();
    }

    @Test
    void namedDiagonalFixtureIsAWin() {
        assertThat(TicTacToeMain.isWin(TicTacToeFixtures.DIAGONAL_WIN_CIRCLE, Stone.CIRCLE)).isTrue();
    }

    @Test
    void drawFixtureHasNoWinner() {
        assertThat(TicTacToeMain.isWin(TicTacToeFixtures.FULL_BOARD_DRAW, Stone.CROSS)).isFalse();
        assertThat(TicTacToeMain.isWin(TicTacToeFixtures.FULL_BOARD_DRAW, Stone.CIRCLE)).isFalse();
    }

    @Test
    void greedyPlayerSelectsFirstEmptyField() {
        Stone[] board = {
                Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
                null, null, null,
                null, null, null
        };

        int selectedField = new GreedyPlayer().play(board, Stone.CIRCLE);

        assertThat(selectedField).isEqualTo(3);
    }

    @Test
    void greedyPlayerThrowsWhenBoardIsFull() {
        Stone[] board = TicTacToeFixtures.copyOf(TicTacToeFixtures.FULL_BOARD_DRAW);

        assertThatThrownBy(() -> new GreedyPlayer().play(board, Stone.CROSS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot play");
    }

    @Test
    void playRejectsSamePlayerInstanceForBothSides() {
        GreedyPlayer player = new GreedyPlayer();

        assertThatThrownBy(() -> TicTacToeMain.play(player, player))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("players must differ");
    }

    @Test
    void playRejectsOutOfRangeMove() {
        QueuePlayer brokenPlayer = new QueuePlayer(-1);

        assertThatThrownBy(() -> TicTacToeMain.play(brokenPlayer, new GreedyPlayer()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot play to position -1");
    }

    @Test
    void playRejectsOccupiedField() {
        QueuePlayer cross = new QueuePlayer(0);
        QueuePlayer circle = new QueuePlayer(0);

        assertThatThrownBy(() -> TicTacToeMain.play(cross, circle))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot play to position 0");
    }

    @Test
    void crossCanWinACompleteGame() {
        QueuePlayer cross = new QueuePlayer(0, 1, 2);
        QueuePlayer circle = new QueuePlayer(3, 4);

        Stone winner = TicTacToeMain.play(cross, circle);

        assertThat(winner).isEqualTo(Stone.CROSS);
    }

    @Test
    void circleCanWinACompleteGame() {
        QueuePlayer cross = new QueuePlayer(0, 1, 8);
        QueuePlayer circle = new QueuePlayer(3, 4, 5);

        Stone winner = TicTacToeMain.play(cross, circle);

        assertThat(winner).isEqualTo(Stone.CIRCLE);
    }

    @Test
    void gameCanEndInADraw() {
        QueuePlayer cross = new QueuePlayer(0, 2, 3, 7, 8);
        QueuePlayer circle = new QueuePlayer(1, 4, 5, 6);

        Stone winner = TicTacToeMain.play(cross, circle);

        assertThat(winner).isNull();
    }

    @Test
    void boardRenderingContainsStonesAndEmptyPosition() {
        Stone[] board = {
                Stone.CROSS, Stone.CIRCLE, null,
                null, null, null,
                null, null, null
        };

        String rendered = TicTacToeMain.toString(board);

        assertThat(rendered)
                .contains("X")
                .contains("O")
                .contains("2");
    }

    @Test
    void humanPlayerReadsPositionFromStandardInput() {
        InputStream originalInput = System.in;

        try {
            System.setIn(new ByteArrayInputStream("4\n".getBytes(StandardCharsets.UTF_8)));

            int selectedField = new HumanPlayer().play(new Stone[TicTacToeMain.BOARD_SIZE], Stone.CROSS);

            assertThat(selectedField).isEqualTo(4);
        } finally {
            System.setIn(originalInput);
        }
    }
}
