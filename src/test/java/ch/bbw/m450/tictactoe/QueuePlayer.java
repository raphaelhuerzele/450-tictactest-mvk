package ch.bbw.m450.tictactoe;

import java.util.ArrayDeque;
import java.util.Deque;

final class QueuePlayer implements TicTacToePlayer {

    private final Deque<Integer> moves = new ArrayDeque<>();

    QueuePlayer(int... moves) {
        for (int move : moves) {
            this.moves.add(move);
        }
    }

    @Override
    public int play(Stone[] board, Stone colorToPlay) {
        Integer move = moves.poll();
        if (move == null) {
            throw new IllegalStateException("No configured move left");
        }
        return move;
    }
}
