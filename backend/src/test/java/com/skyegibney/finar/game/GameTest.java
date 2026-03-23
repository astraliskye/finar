package com.skyegibney.finar.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        // Don't shuffle so alice is always player 1 (index 0) and bob is player 2 (index 1).
        game = new Game("alice", "bob");
    }

    // -------------------------------------------------------------------------
    // checkFinar – no win
    // -------------------------------------------------------------------------

    @Test
    void checkFinar_emptyBoard_noFinar() {
        game.checkFinar();

        assertThat(game.isFinar()).isFalse();
        assertThat(game.getWinner()).isNull();
    }

    @Test
    void checkFinar_fourInARow_noFinar() {
        // Player 1 has four consecutive cells in row 0 – not enough for a win.
        int[] board = game.getBoard();
        board[0] = 1;
        board[1] = 1;
        board[2] = 1;
        board[3] = 1;

        game.checkFinar();

        assertThat(game.isFinar()).isFalse();
    }

    // -------------------------------------------------------------------------
    // checkFinar – horizontal wins
    // -------------------------------------------------------------------------

    @Test
    void checkFinar_horizontalWin_player1() {
        int[] board = game.getBoard();
        for (int i = 0; i < 5; i++) board[i] = 1;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("alice");
        assertThat(game.getWinningMoves()).containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    void checkFinar_horizontalWin_player2() {
        int[] board = game.getBoard();
        for (int i = 0; i < 5; i++) board[i] = 2;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("bob");
        assertThat(game.getWinningMoves()).containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    void checkFinar_horizontalWin_lastRowPlayer1() {
        // Win in the last row (cells 90–94).
        int[] board = game.getBoard();
        for (int i = 90; i < 95; i++) board[i] = 1;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("alice");
    }

    /**
     * Cells 8, 9, 10, 11, 12 span a row boundary (row 0 and row 1).
     * The guard Math.floorDiv(i,10) == Math.floorDiv(i+4,10) must prevent a false win.
     */
    @Test
    void checkFinar_horizontalSpanningRowBoundary_noFinar() {
        int[] board = game.getBoard();
        // Set cells 8,9,10,11,12 to player 1 values.
        board[8] = 1;
        board[9] = 1;
        board[10] = 1;
        board[11] = 1;
        board[12] = 1;

        game.checkFinar();

        assertThat(game.isFinar()).isFalse();
    }

    // -------------------------------------------------------------------------
    // checkFinar – vertical wins
    // -------------------------------------------------------------------------

    @Test
    void checkFinar_verticalWin_player1() {
        // Cells 0, 10, 20, 30, 40 form a vertical line in column 0.
        int[] board = game.getBoard();
        for (int i = 0; i < 5; i++) board[i * 10] = 1;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("alice");
        assertThat(game.getWinningMoves()).containsExactly(0, 10, 20, 30, 40);
    }

    @Test
    void checkFinar_verticalWin_player2() {
        int[] board = game.getBoard();
        for (int i = 0; i < 5; i++) board[i * 10] = 2;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("bob");
        assertThat(game.getWinningMoves()).containsExactly(0, 10, 20, 30, 40);
    }

    // -------------------------------------------------------------------------
    // checkFinar – down-right diagonal wins
    // -------------------------------------------------------------------------

    @Test
    void checkFinar_diagonalDownRight_player1() {
        // Starting at cell 0: 0, 11, 22, 33, 44.
        int[] board = game.getBoard();
        for (int i = 0; i < 5; i++) board[i * 11] = 1;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("alice");
        assertThat(game.getWinningMoves()).containsExactly(0, 11, 22, 33, 44);
    }

    @Test
    void checkFinar_diagonalDownRight_player2() {
        int[] board = game.getBoard();
        for (int i = 0; i < 5; i++) board[i * 11] = 2;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("bob");
    }

    // -------------------------------------------------------------------------
    // checkFinar – down-left diagonal wins
    // -------------------------------------------------------------------------

    @Test
    void checkFinar_diagonalDownLeft_player1() {
        // Starting at cell 4 (column 4, row 0): 4, 13, 22, 31, 40.
        int[] board = game.getBoard();
        int[] cells = {4, 13, 22, 31, 40};
        for (int c : cells) board[c] = 1;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("alice");
        assertThat(game.getWinningMoves()).containsExactly(4, 13, 22, 31, 40);
    }

    @Test
    void checkFinar_diagonalDownLeft_player2() {
        int[] board = game.getBoard();
        int[] cells = {4, 13, 22, 31, 40};
        for (int c : cells) board[c] = 2;

        game.checkFinar();

        assertThat(game.isFinar()).isTrue();
        assertThat(game.getWinner()).isEqualTo("bob");
    }

    // -------------------------------------------------------------------------
    // makeMove – basic behaviour
    // -------------------------------------------------------------------------

    @Test
    void makeMove_firstMove_setsPlayer1PieceOnBoard() {
        game.makeMove(5);

        assertThat(game.getBoard()[5]).isEqualTo(1);
    }

    @Test
    void makeMove_secondMove_setsPlayer2PieceOnBoard() {
        game.makeMove(0);
        game.makeMove(5);

        assertThat(game.getBoard()[5]).isEqualTo(2);
    }

    @Test
    void makeMove_alternatesTurns() {
        // currentMove starts at 0 (alice). After a move it should be 1 (bob).
        assertThat(game.getCurrentTurn()).isEqualTo("alice");

        game.makeMove(0);
        assertThat(game.getCurrentTurn()).isEqualTo("bob");

        game.makeMove(1);
        assertThat(game.getCurrentTurn()).isEqualTo("alice");
    }

    @Test
    void makeMove_recordsMoveInList() {
        game.makeMove(42);

        assertThat(game.getMoves()).containsExactly(42);
    }

    @Test
    void makeMove_outOfBounds_throwsArrayIndexOutOfBoundsException() {
        // Cell 100 is out of the 100-element board (valid indices: 0–99).
        // This test documents the existing out-of-bounds bug: GameService.makeMove
        // guards with n > board.length (i.e. n > 100) rather than n >= board.length,
        // so index 100 slips through and causes an exception here.
        assertThatThrownBy(() -> game.makeMove(100))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    // -------------------------------------------------------------------------
    // getPlayerOne / getPlayerTwo / getCurrentTurn helpers
    // -------------------------------------------------------------------------

    @Test
    void getPlayerOne_returnsFirstPlayer() {
        assertThat(game.getPlayerOne()).isEqualTo("alice");
    }

    @Test
    void getPlayerTwo_returnsSecondPlayer() {
        assertThat(game.getPlayerTwo()).isEqualTo("bob");
    }

    @Test
    void getCurrentTurn_initiallyPlayer1() {
        assertThat(game.getCurrentTurn()).isEqualTo("alice");
    }
}
