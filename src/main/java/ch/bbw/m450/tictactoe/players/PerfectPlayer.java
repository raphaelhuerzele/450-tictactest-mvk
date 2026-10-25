package ch.bbw.m450.tictactoe.players;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

import java.util.Arrays;
import java.util.Objects;

/** An optimal player using negamax: win if possible, otherwise force a draw. */
public final class PerfectPlayer implements TicTacToePlayer {

    @Override
    public int play(Stone[] board, Stone colorToPlay) {
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(colorToPlay, "colorToPlay");
        if (board.length != TicTacToeMain.BOARD_SIZE) {
            throw new IllegalArgumentException("board must contain nine fields");
        }
        if (TicTacToeMain.isWin(board, Stone.CROSS) || TicTacToeMain.isWin(board, Stone.CIRCLE)) {
            throw new IllegalStateException("game is already over");
        }

        var workingBoard = Arrays.copyOf(board, board.length);
        var bestMove = -1;
        var bestScore = -2;
        for (var position = 0; position < workingBoard.length; position++) {
            if (workingBoard[position] == null) {
                workingBoard[position] = colorToPlay;
                var score = -score(workingBoard, colorToPlay.opponent());
                workingBoard[position] = null;
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = position;
                }
            }
        }
        if (bestMove == -1) {
            throw new IllegalStateException("cannot play at all");
        }
        return bestMove;
    }

    /** Scores the board from the next player's perspective: loss -1, draw 0, win 1. */
    private static int score(Stone[] board, Stone color) {
        if (TicTacToeMain.isWin(board, color.opponent())) {
            return -1;
        }
        var best = -2;
        for (var position = 0; position < board.length; position++) {
            if (board[position] == null) {
                board[position] = color;
                var result = -score(board, color.opponent());
                board[position] = null;
                best = Math.max(best, result);
                if (best == 1) {
                    return best;
                }
            }
        }
        return best == -2 ? 0 : best;
    }
}
