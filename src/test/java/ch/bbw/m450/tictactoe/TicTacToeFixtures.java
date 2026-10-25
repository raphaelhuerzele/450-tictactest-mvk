package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

final class TicTacToeFixtures {

    static final Stone[] FULL_BOARD_DRAW = {
            Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
            Stone.CROSS, Stone.CIRCLE, Stone.CIRCLE,
            Stone.CIRCLE, Stone.CROSS, Stone.CROSS
    };

    private TicTacToeFixtures() {
    }

    static Stone[] boardWithWinningLine(int[] positions, Stone stone) {
        var board = new Stone[TicTacToeMain.BOARD_SIZE];
        for (var position : positions) {
            board[position] = stone;
        }
        return board;
    }
}
