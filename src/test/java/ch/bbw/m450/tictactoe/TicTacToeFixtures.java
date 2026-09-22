package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

import java.util.Arrays;

final class TicTacToeFixtures {

    static final Stone[] ROW_WIN_CROSS = {
            Stone.CROSS, Stone.CROSS, Stone.CROSS,
            null, Stone.CIRCLE, null,
            Stone.CIRCLE, null, null
    };

    static final Stone[] DIAGONAL_WIN_CIRCLE = {
            Stone.CIRCLE, Stone.CROSS, null,
            Stone.CROSS, Stone.CIRCLE, null,
            null, null, Stone.CIRCLE
    };

    static final Stone[] FULL_BOARD_DRAW = {
            Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
            Stone.CROSS, Stone.CIRCLE, Stone.CIRCLE,
            Stone.CIRCLE, Stone.CROSS, Stone.CROSS
    };

    private TicTacToeFixtures() {
    }

    static Stone[] boardWithWinningLine(int[] positions, Stone stone) {
        Stone[] board = new Stone[TicTacToeMain.BOARD_SIZE];
        for (int position : positions) {
            board[position] = stone;
        }
        return board;
    }

    static Stone[] copyOf(Stone[] board) {
        return Arrays.copyOf(board, board.length);
    }
}
