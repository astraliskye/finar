type BoardProps = {
    moves: number[],
    winningMoves: number[]
    onCellClick: (n: number) => void
}

function Board({ moves, winningMoves, onCellClick }: BoardProps) {
    return <div className="flex flex-col gap-1">
        {[...new Array(10)].map((_, i) => <div key={i} className="flex gap-1">
            {[...new Array(10)].map((_, j) => {
                let cellIndex = moves.indexOf(j + i * 10);
                let actualCellIndex = j + i * 10;

                return <div key={j} className={`w-8 h-8 gap-1 flex items-center justify-center font-bold font-mono select-none transition ${winningMoves.includes(actualCellIndex) ? "bg-red-500 " : cellIndex !== -1 && cellIndex === moves.length - 1 ? "bg-stone-600 " : "bg-stone-800 "}`}
                onClick={() => onCellClick(j + i * 10)}>
                {cellIndex != -1 && (
                    cellIndex % 2 === 0
                        ? "X"
                        : "O"
                )}
            </div>})}
        </div>)}
    </div>;
}

export default Board;
