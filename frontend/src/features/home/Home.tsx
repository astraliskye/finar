import { useContext, useEffect, useState } from "react"
import { useMutation } from "@tanstack/react-query";
import { useNavigate, useSearchParams } from "react-router-dom";
import { WebSocketContext } from "@contexts/WebSocketContext";
import { useMeQuery } from "@hooks/useMeQuery";

function Home() {
    const [searching, setSearching] = useState(false);
    const navigate = useNavigate();
    const { send, setMessageCallback } = useContext(WebSocketContext);
    const { data, isError } = useMeQuery();
    const [showInstructions, setShowInstructions] = useState(false);
    const [searchParams, _] = useSearchParams();

    useEffect(() => {
        setMessageCallback((message: MessageEvent) => {
            const body = JSON.parse(message.data) as { type: string, data: string };
            console.log(message.data);
            console.log(body);

            switch (body.type) {
                case "matchFound":
                    const gameId = body.data as string;
                    navigate(`/game/${gameId}`);
                    break;
                case "ack":
                    const type = body.data as string;

                    switch (type) {
                        case "joinQueue":
                            setSearching(true);
                            break;
                        case "cancelQueue":
                            setSearching(false);
                            break;
                    }
                    break;
                default:
                    break;
            }
        });
    });

    const createLobbyMutation = useMutation({
        mutationFn: async () => {
            const response = await fetch("/api/lobby", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            const body = await response.json() as {
                lobbyId: string
            };

            if (response.status === 200 && body.lobbyId !== "-1") {
                navigate(`/lobby/${body.lobbyId}`);
            }
        }
    })

    if (isError || !data) {
        return <div className="w-11/12 max-w-[600px] mx-auto mb-16">
            <div>
                <h1 className="text-6xl pt-48 mb-4 text-primary tracking-widest font-bold">finar</h1>
                <p className="text-lg">A game where the only goal is to make five in a row</p>
            </div>
            <div>
                <h2 className="font-bold text-4xl mt-32 mb-8">Playing the Game</h2>
                <p className="text-lg mt-8">
                    Have you ever played tic-tac-toe? It's kind of like that.
                    Two players take turns placing symbols on a board and try to
                    get their symbols to line up. Your goal is to line up your
                    symbols while also preventing your opponent from lining up
                    too many symbols. Lines can be made horizontally, vertically,
                    or diagonally.
                </p>
                <p className="text-lg mt-8">
                    But don't take too long! You have a limited amount of time
                    you can spend on your turns. You and your opponent each start
                    with a timer that has a certain amount of time on it. During
                    your turn, your timer counts down, but it is paused while it
                    is your opponent's turn. The timers only start once both
                    players have made their first move.
                </p>
            </div>
            <div>
                <h2 className="font-bold text-4xl mt-32 pb-8">Winning the Game</h2>
                <p className="text-lg">
                    finar stands for <span className="text-primary font-bold">f</span>ive-
                    <span className="text-primary font-bold">in</span>-
                    <span className="text-primary font-bold">a</span>-
                    <span className="text-primary font-bold">r</span>ow. The goal of the
                    game is to get <span className="underline">at least</span> five
                    of your symbols in a row. The first player to achieve this
                    wins. However, if your timer runs out before either player has
                    achieved this goal, then your opponent wins, so think fast!
                </p>
                <p className="text-lg mt-8">
                    The game ends when it detects five symbols in a row, but if
                    you're crafty, you can exceed that limit. See how many you
                    can get in a row!
                </p>
            </div>
            <footer className="h-32 flex items-center justify-center">
                © 2025
            </footer>
        </div>
    }

    return (
        <div>
            {searching && <div className="fixed top-0 left-0 w-screen h-screen bg-black bg-opacity-50 z-10 flex items-center justify-center">
                <div className="w-32 bg-stone-900 rounded-2xl flex flex-col gap-4 py-4 items-center justify-center">
                    <svg width="64" height="64" stroke="#fff" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><g><circle cx="12" cy="12" r="9.5" fill="none" strokeWidth="3" strokeLinecap="round"><animate attributeName="stroke-dasharray" dur="1.5s" calcMode="spline" values="0 150;42 150;42 150;42 150" keyTimes="0;0.475;0.95;1" keySplines="0.42,0,0.58,1;0.42,0,0.58,1;0.42,0,0.58,1" repeatCount="indefinite" /><animate attributeName="stroke-dashoffset" dur="1.5s" calcMode="spline" values="0;-16;-59;-59" keyTimes="0;0.475;0.95;1" keySplines="0.42,0,0.58,1;0.42,0,0.58,1;0.42,0,0.58,1" repeatCount="indefinite" /></circle><animateTransform attributeName="transform" type="rotate" dur="2s" values="0 12 12;360 12 12" repeatCount="indefinite" /></g></svg>
                    <button onClick={() => send({ type: "cancelQueue" })}>Cancel</button>
                </div>
            </div>}
            {showInstructions && <div className="fixed top-0 left-0 w-screen h-screen bg-black bg-opacity-50 z-10 flex justify-center py-8"
                onClick={() => setShowInstructions(false)}>
                <div className={"transition-all w-11/12 max-w-lg bg-stone-900 rounded-xl overflow-y-scroll px-4 py-4 flex flex-col "
                    + (showInstructions ? "opacity-100" : "opacity-0")}
                    onClick={(e) => e.stopPropagation()}>
                    <button className="underline" onClick={() => setShowInstructions(false)}>Dismiss</button>
                    <div>
                        <h2 className="font-bold text-4xl mt-4 mb-8">Playing the Game</h2>
                        <p className="text-lg mt-8">
                            Have you ever played tic-tac-toe? It's kind of like that.
                            Two players take turns placing symbols on a board and try to
                            get their symbols to line up. Your goal is to line up your
                            symbols while also preventing your opponent from lining up
                            too many symbols. Lines can be made horizontally, vertically,
                            or diagonally.
                        </p>
                        <p className="text-lg mt-8">
                            But don't take too long! You have a limited amount of time
                            you can spend on your turns. You and your opponent each start
                            with a timer that has a certain amount of time on it. During
                            your turn, your timer counts down, but it is paused while it
                            is your opponent's turn. The timers only start once both
                            players have made their first move.
                        </p>
                    </div>
                    <div>
                        <h2 className="font-bold text-4xl mt-32 pb-8">Winning the Game</h2>
                        <p className="text-lg">
                            finar stands for <span className="text-primary font-bold">f</span>ive-
                            <span className="text-primary font-bold">in</span>-
                            <span className="text-primary font-bold">a</span>-
                            <span className="text-primary font-bold">r</span>ow. The goal of the
                            game is to get <span className="underline">at least</span> five
                            of your symbols in a row. The first player to achieve this
                            wins. However, if your timer runs out before either player has
                            achieved this goal, then your opponent wins, so think fast!
                        </p>
                        <p className="text-lg mt-8">
                            The game ends when it detects five symbols in a row, but if
                            you're crafty, you can exceed that limit. See how many you
                            can get in a row!
                        </p>
                    </div>

                </div>
            </div>}
            <div className="pt-48">
                <div className="w-11/12 mx-auto max-w-2xl">
                    <h1 className="text-6xl mb-4 text-primary tracking-widest font-bold">finar</h1>
                    <p className="mb-24">A game where the only goal is to make five in a row.</p>
                </div>
                {searchParams.has("error") && <p className="text-red-500 text-center mb-4">
                    {searchParams.get("error") === "playerKicked" && "You have been kicked from the lobby."}
                    {searchParams.get("error") === "lobbyDisbanded" && "The lobby has been disbanded."}
                </p>}
                <div className={`flex flex-col items-center gap-4 rounded-lg`}>
                    <button className="select-none px-3 py-2 bg-primary text-black rounded-md disabled:opacity-50"
                        onClick={() => send({ type: "joinQueue" })}>
                        Quick Match
                    </button>
                    <button className="select-none px-3 py-2 text-white rounded-md bg-stone-800 hover:bg-stone-700 transition disabled:opacity-50 disabled:hover:bg-stone-800"
                        onClick={() => createLobbyMutation.mutate()}>
                        Create Lobby
                    </button>
                    <button className="select-none px-3 py-2 text-white rounded-md bg-stone-800 hover:bg-stone-700 transition disabled:opacity-50 disabled:hover:bg-stone-800"
                        onClick={() => setShowInstructions(true)}>
                        How to Play
                    </button>
                </div>
            </div>
            <footer className="h-32 flex items-center justify-center">
                © 2025
            </footer>
        </div>
    );
}

export default Home;
