package ch.bbw.m450.tictactoe;

public interface TicTacToePlayer {

	/**
	 * Decide on the next move on a partially played TicTacToe-board.
	 *
	 * @param board the current board to play on
	 * @param colorToPlay my own color
	 * @return the position to play to (0-8), where 0 is top-left
	 */
	int play(Stone[] board, Stone colorToPlay);

	/**
	 * Represents the state of a single piece/field on the TicTacToe-board.
	 */
	enum Stone {
		CROSS, CIRCLE;

		public Stone opponent() {
			return this == Stone.CROSS ? Stone.CIRCLE : Stone.CROSS;
		}
	}
}
