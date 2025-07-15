import { Link, useNavigate } from "react-router-dom";
import Board from "./components/Board";
import { useCallback, useContext, useEffect, useState } from "react";
import useTimer from "./hooks/useTimer";
import { WebSocketContext } from "@contexts/WebSocketContext";
import { useMeQuery } from "@hooks/useMeQuery";
import Chat from "@components/Chat";

function Play() {
    const navigate = useNavigate();
    const [initialJoinReceived, setInitialJoinReceived] = useState(false);
    const [player, setPlayer] = useState("");
    const [opponent, setOpponent] = useState("");
    const [turn, setTurn] = useState(-1);
    const [moves, setMoves] = useState<number[]>([]);
    const [gameId, setGameId] = useState("");
    const [wins, setWins] = useState(0);
    const [draws, setDraws] = useState(0);
    const [losses, setLosses] = useState(0);
    const [winningMoves, setWinningMoves] = useState<number[]>([]);
    const { send, setMessageCallback, connected } = useContext(WebSocketContext);
    const { isLoading: authLoading, isError: authError } = useMeQuery();
    const [messages, setMessages] = useState<{
        username: string,
        content: string
    }[]>([]);

    const {
        time: p1Time,
        start: startP1Timer,
        stop: stopP1Timer,
        set: setP1Timer
    } = useTimer(0);
    const {
        time: p2Time,
        start: startP2Timer,
        stop: stopP2Timer,
        set: setP2Timer
    } = useTimer(0);

    const [gameOver, setGameOver] = useState(false);
    const [gameOverState, setGameOverState] = useState("");
    const [winner, setWinner] = useState("");

    useEffect(() => {
        if (authError) {
            navigate(`/login?redirectTo=/game/${gameId}`);
        }
    }, [authLoading, authError]);

    const messageCallback = useCallback((message: MessageEvent) => {
        const body = JSON.parse(message.data) as { type: string, data: any };
        console.log(body);

        switch (body.type) {
            case "redirect":
                const dest = body.data as string;
                navigate(dest);
                break;
            case "initialJoin":
                const initialJoinData = body.data as {
                    gameId: string,
                    player: string,
                    opponent: string,
                    wins: number,
                    draws: number,
                    losses: number,
                    turn: number,
                    moves: string,
                    timeControl: {
                        player1Time: number,
                        player2Time: number
                    }
                };

                setGameId(initialJoinData.gameId);

                if (initialJoinData.moves !== "") {
                    const newMoves = initialJoinData.moves.split(",")
                        .map(n => Number.parseInt(n))
                        .filter(n => n !== Number.NaN);

                    setMoves(_ => {
                        let nMoves = newMoves.length;

                        if (nMoves > 1) {
                            if (nMoves % 2 === 0) {
                                startP1Timer();
                                stopP2Timer();
                            } else if (nMoves % 2 === 1) {
                                startP2Timer();
                                stopP1Timer();
                            }
                        }


                        return [...newMoves];
                    });

                    setP1Timer(initialJoinData.timeControl.player1Time);
                    setP2Timer(initialJoinData.timeControl.player2Time);
                }

                setPlayer(initialJoinData.player);
                setOpponent(initialJoinData.opponent);
                setTurn(initialJoinData.turn);
                setWins(initialJoinData.wins);
                setDraws(initialJoinData.draws);
                setLosses(initialJoinData.losses);
                setP1Timer(initialJoinData.timeControl.player1Time);
                setP2Timer(initialJoinData.timeControl.player2Time);
                setInitialJoinReceived(true);
                break;
            case "gameChat":
                const newMessage = body.data as {
                    username: string,
                    content: string
                };

                setMessages(prevMessages => [
                    ...prevMessages,
                    newMessage
                ]);
                break;
            case "move":
                const moveData = body.data as {
                    player: string,
                    n: number,
                    timeControl: {
                        player1Time: number,
                        player2Time: number
                    }
                };

                setMoves(prevMoves => {
                    let nMoves = prevMoves.length + 1;

                    if (nMoves > 1) {
                        if (nMoves % 2 === 0) {
                            startP1Timer();
                            stopP2Timer();
                        } else if (nMoves % 2 === 1) {
                            startP2Timer();
                            stopP1Timer();
                        }
                    }


                    return [...prevMoves, moveData.n];
                });

                setP1Timer(moveData.timeControl.player1Time);
                setP2Timer(moveData.timeControl.player2Time);
                break;
            case "gameOver":
                const gameOverData = body.data as {
                    reason: string,
                    winner: string,
                };

                setGameOverState(gameOverData.reason.toLowerCase())
                setWinner(gameOverData.winner);
                stopP1Timer();
                stopP2Timer();

                setGameOver(true);
                break;
            case "finarGameOver":
                const finarGameOverData = body.data as {
                    reason: string,
                    winner: string,
                    winningMoves: string
                };

                setGameOverState(finarGameOverData.reason.toLowerCase())
                setWinner(finarGameOverData.winner);
                setWinningMoves(finarGameOverData
                    .winningMoves
                    .split(",")
                    .map(m => parseInt(m)));
                stopP1Timer();
                stopP2Timer();

                setTimeout(() => {
                    setGameOver(true);
                }, 2000);
                break;

            case "timeUpdate":
                const timeUpdateData = body.data as {
                    player1Time: number,
                    player2Time: number
                }

                setP1Timer(timeUpdateData.player1Time);
                setP2Timer(timeUpdateData.player2Time);
                break;
            case "matchNotFound":
                navigate("/?error=matchNotFound");
                break;
            default:
                break;
        }
    }, [moves]);

    useEffect(() => {
        setMessageCallback(messageCallback);
    }, [connected, moves]);


    useEffect(() => {
        if (p1Time < 0 || p2Time < 0) {
            send({ gameId, type: "timeFlag" });
        }
    }, [p1Time, p2Time]);

    useEffect(() => {
        if (connected) {
            send({ type: "joinGame" });
        }
    }, [connected]);

    if ((!connected || !initialJoinReceived) && !gameOver) {
        return <div className="w-screen h-screen flex items-center justify-center">
            <svg width="64" height="64" stroke="#fff" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><g><circle cx="12" cy="12" r="9.5" fill="none" strokeWidth="3" strokeLinecap="round"><animate attributeName="stroke-dasharray" dur="1.5s" calcMode="spline" values="0 150;42 150;42 150;42 150" keyTimes="0;0.475;0.95;1" keySplines="0.42,0,0.58,1;0.42,0,0.58,1;0.42,0,0.58,1" repeatCount="indefinite" /><animate attributeName="stroke-dashoffset" dur="1.5s" calcMode="spline" values="0;-16;-59;-59" keyTimes="0;0.475;0.95;1" keySplines="0.42,0,0.58,1;0.42,0,0.58,1;0.42,0,0.58,1" repeatCount="indefinite" /></circle><animateTransform attributeName="transform" type="rotate" dur="2s" values="0 12 12;360 12 12" repeatCount="indefinite" /></g></svg>
        </div>
    } else {
        return (
            <>
                {gameOver &&
                    <div className="fixed top-0 left-0 w-screen h-screen bg-black bg-opacity-50 z-10 flex items-center justify-center">
                        <div className="w-64 h-64 flex flex-col items-center justify-center gap-8 bg-stone-900 rounded-xl">
                            <div className="flex flex-col items-center">
                                <h2 className="text-2xl">Game Over</h2>
                                {(gameOverState === "abandon" || gameOverState === "finar" || gameOverState === "timeout") && <p><span className="text-primary px-1">{winner}</span> wins by <span className="text-primary px-1">{gameOverState}</span></p>}
                                {(gameOverState === "draw" || gameOverState === "abort") && <p>Game ended in a <span className="text-primary px-1">{gameOverState}</span></p>}
                            </div>
                            <div className="flex flex-col items-center">
                                <Link to="/" className="underline">Go to Main Menu</Link>
                            </div>
                        </div>
                    </div>
                }
                <div className="py-4 flex flex-col items-center gap-16">
                    <button onClick={() => { send({ gameId, type: "quit" }) }}>
                        {moves.length < 2 ? "Abort" : "Abandon"}
                    </button>
                    <div className="flex flex-col gap-8 items-center">
                        <div className="flex justify-around w-64">
                            <p className={turn === moves.length % 2 ? "text-primary" : ""}>{player}</p>
                            <span>{turn === 0 ? (p1Time > 10000 ? Math.floor(p1Time / 1000) : (p1Time / 1000).toFixed(1)) : (p2Time > 10000 ? Math.floor(p2Time / 1000) : (p2Time / 1000).toFixed(1))}</span>
                            vs
                            <p className={turn !== moves.length % 2 ? "text-primary" : ""}>{opponent}</p>
                            <span>{turn === 1 ? (p1Time > 10000 ? Math.floor(p1Time / 1000) : (p1Time / 1000).toFixed(1)) : (p2Time > 10000 ? Math.floor(p2Time / 1000) : (p2Time / 1000).toFixed(1))}</span>
                        </div>
                        <p>
                            <span className="text-lime-500">{wins}</span> - {draws} - <span className="text-red-500">{losses}</span>
                        </p>
                        <div className="flex md:flex-row gap-10 flex-col items-center">
                            <Board winningMoves={winningMoves} moves={moves} onCellClick={(n: number) => {
                                send({
                                    gameId,
                                    type: "move",
                                    data: n
                                })
                            }} />
                            <Chat messages={messages} sendChatMessage={(message: string) => send({
                                gameId,
                                type: "gameChat",
                                data: message
                            })} />
                        </div>
                    </div>
                </div >
            </>
        );
    }
}

export default Play;
