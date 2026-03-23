import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Board from './Board';

describe('Board', () => {
    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    it('renders 100 cells', () => {
        render(<Board moves={[]} winningMoves={[]} onCellClick={vi.fn()} />);

        // Every cell renders as a div; we count by aria role is tricky without roles,
        // so query by the shared class substring.
        const cells = document.querySelectorAll('.w-8');
        expect(cells).toHaveLength(100);
    });

    it('renders 10 rows of 10 cells each', () => {
        render(<Board moves={[]} winningMoves={[]} onCellClick={vi.fn()} />);

        // The outer container is the only element with flex-col; its direct children
        // are the 10 row divs.
        const rows = document.querySelectorAll('.flex-col > div');
        expect(rows).toHaveLength(10);
        rows.forEach(row => {
            expect(row.querySelectorAll('.w-8')).toHaveLength(10);
        });
    });

    it('empty board shows no piece markers', () => {
        render(<Board moves={[]} winningMoves={[]} onCellClick={vi.fn()} />);

        expect(screen.queryByText('X')).toBeNull();
        expect(screen.queryByText('O')).toBeNull();
    });

    // -------------------------------------------------------------------------
    // Piece display
    // -------------------------------------------------------------------------

    it('first move (even index 0) renders X', () => {
        // moves[0] = 5 means cell 5 received the first move.
        render(<Board moves={[5]} winningMoves={[]} onCellClick={vi.fn()} />);

        expect(screen.getByText('X')).toBeInTheDocument();
    });

    it('second move (odd index 1) renders O', () => {
        // Cell 5 is move 0 (X), cell 6 is move 1 (O).
        render(<Board moves={[5, 6]} winningMoves={[]} onCellClick={vi.fn()} />);

        expect(screen.getByText('X')).toBeInTheDocument();
        expect(screen.getByText('O')).toBeInTheDocument();
    });

    it('displays alternating X and O for multiple moves', () => {
        // Moves at cells 0,1,2,3,4 → X,O,X,O,X
        render(<Board moves={[0, 1, 2, 3, 4]} winningMoves={[]} onCellClick={vi.fn()} />);

        const xs = screen.getAllByText('X');
        const os = screen.getAllByText('O');
        expect(xs).toHaveLength(3); // indices 0, 2, 4
        expect(os).toHaveLength(2); // indices 1, 3
    });

    // -------------------------------------------------------------------------
    // Winning move highlighting
    // -------------------------------------------------------------------------

    it('winning cells get the red highlight class', () => {
        render(<Board moves={[0, 1, 2, 3, 4]} winningMoves={[0, 1, 2, 3, 4]} onCellClick={vi.fn()} />);

        // All five winning cells should have bg-red-500.
        const highlighted = document.querySelectorAll('.bg-red-500');
        expect(highlighted).toHaveLength(5);
    });

    it('non-winning occupied cells do not have the red highlight', () => {
        // Only cell 0 is a winning move; cells 1-4 are ordinary moves.
        render(<Board moves={[0, 1, 2, 3, 4]} winningMoves={[0]} onCellClick={vi.fn()} />);

        const highlighted = document.querySelectorAll('.bg-red-500');
        expect(highlighted).toHaveLength(1);
    });

    it('the most-recent move gets the darker highlight class', () => {
        // The last element in moves[] is the most-recent move and gets bg-stone-600.
        render(<Board moves={[0, 5]} winningMoves={[]} onCellClick={vi.fn()} />);

        const recentCell = document.querySelectorAll('.bg-stone-600');
        expect(recentCell).toHaveLength(1);
    });

    // -------------------------------------------------------------------------
    // Click handler
    // -------------------------------------------------------------------------

    it('clicking a cell calls onCellClick with the correct index', async () => {
        const user = userEvent.setup();
        const onCellClick = vi.fn();
        render(<Board moves={[]} winningMoves={[]} onCellClick={onCellClick} />);

        // Cell index = col + row * 10. The very first cell (row 0, col 0) = index 0.
        const cells = document.querySelectorAll('.w-8');
        await user.click(cells[0] as HTMLElement);

        expect(onCellClick).toHaveBeenCalledOnce();
        expect(onCellClick).toHaveBeenCalledWith(0);
    });

    it('clicking cell at row 1, col 3 passes index 13', async () => {
        const user = userEvent.setup();
        const onCellClick = vi.fn();
        render(<Board moves={[]} winningMoves={[]} onCellClick={onCellClick} />);

        const cells = document.querySelectorAll('.w-8');
        await user.click(cells[13] as HTMLElement);

        expect(onCellClick).toHaveBeenCalledWith(13);
    });

    it('clicking the last cell (index 99) passes 99', async () => {
        const user = userEvent.setup();
        const onCellClick = vi.fn();
        render(<Board moves={[]} winningMoves={[]} onCellClick={onCellClick} />);

        const cells = document.querySelectorAll('.w-8');
        await user.click(cells[99] as HTMLElement);

        expect(onCellClick).toHaveBeenCalledWith(99);
    });
});
