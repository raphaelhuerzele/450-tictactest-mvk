package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

/** QuickTheories generates boards; fixed seeds make failing examples reproducible. */
class BoardPropertiesTest {

    @Test
    void greedyAlwaysSelectsTheFirstEmptyFieldOnGeneratedBoards() {
        qt().withFixedSeed(450).withExamples(1000)
                .forAll(integers().between(0, 19682))
                .checkAssert(encoded -> {
                    var board = decode(encoded);
                    var before = board.clone();
                    var expected = IntStream.range(0, board.length).filter(i -> board[i] == null).findFirst();
                    if (expected.isPresent()) {
                        assertThat(new GreedyPlayer().play(board, Stone.CROSS)).isEqualTo(expected.getAsInt());
                    } else {
                        var player = new GreedyPlayer();
                        assertThatThrownBy(() -> player.play(board, Stone.CROSS))
                                .isInstanceOf(IllegalStateException.class);
                    }
                    assertThat(board).containsExactly(before);
                });
    }

    @Test
    void swappingBothColorsPreservesWinningLines() {
        qt().withFixedSeed(451).withExamples(1000)
                .forAll(integers().between(0, 19682))
                .checkAssert(encoded -> {
                    var board = decode(encoded);
                    var swapped = Arrays.stream(board).map(stone -> stone == null ? null : stone.opponent())
                            .toArray(Stone[]::new);
                    for (var color : Stone.values()) {
                        assertThat(TicTacToeMain.isWin(swapped, color.opponent()))
                                .isEqualTo(TicTacToeMain.isWin(board, color));
                    }
                });
    }

    private static Stone[] decode(int encoded) {
        var board = new Stone[9];
        var remaining = encoded;
        for (var i = 0; i < board.length; i++) {
            board[i] = switch (remaining % 3) {
                case 1 -> Stone.CROSS;
                case 2 -> Stone.CIRCLE;
                default -> null;
            };
            remaining /= 3;
        }
        return board;
    }
}
