package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TicTacToeTest {

    private GreedyPlayer greedyPlayer;

    @BeforeEach
    void setUp() {
        greedyPlayer = new GreedyPlayer();
    }

    @Test
    void opponentShouldReturnTheOtherStone() {
        assertThat(Stone.CROSS.opponent()).isEqualTo(Stone.CIRCLE);
        assertThat(Stone.CIRCLE.opponent()).isEqualTo(Stone.CROSS);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("winBoardFixtures")
    void shouldDetectWinsForDifferentBoardConfigurations(
            String description,
            Stone[] board,
            Stone stone,
            boolean expectedWin) {

        boolean result = TicTacToeMain.isWin(board, stone);

        assertThat(result).isEqualTo(expectedWin);
    }

    static Stream<Arguments> winBoardFixtures() {
        return Stream.of(
                Arguments.of(
                        "cross wins in top row",
                        board("XXX.O.O.."),
                        Stone.CROSS,
                        true
                ),
                Arguments.of(
                        "cross wins in middle row",
                        board("O..XXX.O."),
                        Stone.CROSS,
                        true
                ),
                Arguments.of(
                        "cross wins in bottom row",
                        board("O.O...XXX"),
                        Stone.CROSS,
                        true
                ),
                Arguments.of(
                        "circle wins in left column",
                        board("OXXO.XO.X"),
                        Stone.CIRCLE,
                        true
                ),
                Arguments.of(
                        "circle wins in middle column",
                        board("XOXXO..O."),
                        Stone.CIRCLE,
                        true
                ),
                Arguments.of(
                        "circle wins in right column",
                        board("XXO.XO..O"),
                        Stone.CIRCLE,
                        true
                ),
                Arguments.of(
                        "circle wins on main diagonal",
                        board("OXX.OX..O"),
                        Stone.CIRCLE,
                        true
                ),
                Arguments.of(
                        "cross wins on anti-diagonal",
                        board("O.X.X.XO."),
                        Stone.CROSS,
                        true
                ),
                Arguments.of(
                        "cross has no winning line",
                        board("XOXOXOOX."),
                        Stone.CROSS,
                        false
                ),
                Arguments.of(
                        "circle has no winning line",
                        board("XOXOXOOX."),
                        Stone.CIRCLE,
                        false
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("greedyPlayerFixtures")
    void greedyPlayerShouldChooseFirstEmptyField(
            String description,
            Stone[] board,
            int expectedField) {

        int selectedField = greedyPlayer.play(board, Stone.CIRCLE);

        assertThat(selectedField).isEqualTo(expectedField);
    }

    static Stream<Arguments> greedyPlayerFixtures() {
        return Stream.of(
                Arguments.of(
                        "first field is empty",
                        board("........."),
                        0
                ),
                Arguments.of(
                        "first three fields are occupied",
                        board("XOX......"),
                        3
                ),
                Arguments.of(
                        "only last field is empty",
                        board("XOXOXOXO."),
                        8
                )
        );
    }

    @Test
    void crossPlayerShouldWinACompleteGame() {
        TicTacToePlayer crossPlayer = playerWithMoves(0, 1, 2);
        TicTacToePlayer circlePlayer = playerWithMoves(3, 4);

        Stone winner = TicTacToeMain.play(crossPlayer, circlePlayer);

        assertThat(winner).isEqualTo(Stone.CROSS);
    }

    private static Stone[] board(String layout) {
        if (layout.length() != TicTacToeMain.BOARD_SIZE) {
            throw new IllegalArgumentException(
                    "Board layout must contain exactly 9 fields"
            );
        }

        Stone[] board = new Stone[TicTacToeMain.BOARD_SIZE];

        for (int i = 0; i < layout.length(); i++) {
            board[i] = switch (layout.charAt(i)) {
                case 'X' -> Stone.CROSS;
                case 'O' -> Stone.CIRCLE;
                case '.' -> null;
                default -> throw new IllegalArgumentException(
                        "Unknown board field: " + layout.charAt(i)
                );
            };
        }

        return board;
    }

    private static TicTacToePlayer playerWithMoves(int... moves) {
        int[] currentMove = {0};

        return (board, color) -> moves[currentMove[0]++];
    }
}