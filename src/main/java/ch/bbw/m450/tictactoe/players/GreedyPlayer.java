package ch.bbw.m450.tictactoe.players;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * Valid (but stupid) player, who always plays as much top-left as possible.
 */
public class GreedyPlayer implements TicTacToePlayer {

	@Override
	public int play(Stone[] board, Stone colorToPlay) {
		for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
			if (board[i] == null) {
				return i;
			}
		}
		throw new IllegalStateException("cannot play at all");
	}
}
