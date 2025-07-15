import Chat from "@components/Chat";
import { useNavigate, useParams } from "react-router-dom";
import PlayerList from "./PlayerList";
import { useContext, useEffect, useState } from "react";
import { WebSocketContext } from "@contexts/WebSocketContext";
import { useMeQuery } from "@hooks/useMeQuery";

function Lobby() {
    const { id } = useParams();;
    const [messages, setMessages] = useState<{
        username: string,
        content: string
    }[]>([]);
    const [players, setPlayers] = useState<{
        username: string,
        lobbyOwner: boolean,
        ready: boolean
    }[]>([]);
    const [owner, setOwner] = useState("");
    const [maxPlayers, setMaxPlayers] = useState(0);
    const navigate = useNavigate();
    const { data, isLoading: authLoading, isError: authError } = useMeQuery();
    const { send, setMessageCallback, connected } = useContext(WebSocketContext);

    useEffect(() => {
        setMessageCallback((messageEvent) => {
            const message = JSON.parse(messageEvent.data) as { type: string, data: any }
            console.log(message);

            switch (message.type) {
                case "lobbyInfo":
                    const lobbyInfo = message.data as {
                        id: string,
                        owner: string,
                        players: {
                            username: string,
                            ready: boolean
                        }[]
                    }
                    setPlayers(_ => lobbyInfo.players.map(p => ({
                        ...p,
                        lobbyOwner: p.username === lobbyInfo.owner,
                    })));
                    setOwner(lobbyInfo.owner);
                    setMaxPlayers(2);
                    break;
                case "playerJoinedLobby":
                    const newPlayer = message.data as string;

                    setPlayers(prevPlayers => [
                        ...prevPlayers,
                        { username: newPlayer, lobbyOwner: newPlayer === owner, ready: false }
                    ]);
                    break;
                case "playerKicked":
                    const playerKickedMessage = message.data as {
                        lobbyId: string,
                        player: string
                    };

                    if (playerKickedMessage.player === data?.username) {
                        navigate("/?error=playerKicked");
                    }

                    if (id === playerKickedMessage.lobbyId) {
                        setPlayers(prevPlayers =>
                            prevPlayers.filter(player =>
                                player.username !== playerKickedMessage.player));
                    }
                    break;
                case "playerLeft":
                    const playerLeftMessage = message.data as {
                        lobbyId: string,
                        player: string
                    };

                    if (id === playerLeftMessage.lobbyId) {
                        setPlayers(prevPlayers =>
                            prevPlayers.filter(player =>
                                player.username !== playerLeftMessage.player));
                    }
                    break;
                case "lobbyDisbanded":
                    const lobbyDisbandedMessage = message.data as {
                        lobbyId: string
                    };

                    if (id === lobbyDisbandedMessage.lobbyId) {
                        navigate("/?error=lobbyDisbanded");
                    }
                    break;
                case "playerReadyStatus":
                    const readyStatus = message.data as {
                        username: string,
                        ready: boolean
                    };

                    setPlayers(prevPlayers => prevPlayers.map(player => {
                        if (player.username !== readyStatus.username) {
                            return player;
                        } else {
                            return {
                                ...player,
                                ready: readyStatus.ready
                            };
                        }
                    }));

                    break;
                case "lobbyChat":
                    const newMessage = message.data as {
                        username: string,
                        content: string
                    };

                    setMessages(prevMessages => [
                        ...prevMessages,
                        newMessage
                    ]);
                    break;
                case "matchFound":
                    const gameId = message.data as string;
                    navigate(`/game/${gameId}`)
                    break;
                case "redirect":
                    const dest = message.data as string;
                    navigate(dest);
                    break;
                case "lobbyNotFound":
                    navigate("/?error=lobbyNotFound");
                    break;
                default:
                    return;
            }
        });
    }, [connected]);

    useEffect(() => {
        if (connected) {
            send({
                lobbyId: id,
                type: "joinLobby",
            });
        }
    }, [connected]);

    useEffect(() => {
        if (authError) {
            navigate(`/login?redirectTo=/lobby/${id}`);
        }
    }, [authLoading, authError]);

    return <div className="h-screen">
        <h1 className="text-center m-16 text-2xl">{owner}'s lobby</h1>
        <div className="flex md:flex-row gap-6 md:justify-center md:h-2/3 md:px-8 px-4 flex-col">
            <div className="md:w-3/12 flex flex-col bg-stone-900 rounded-lg p-6 h-full justify-between">
                <div className="flex flex-col justify-between gap-8 h-full">
                    <PlayerList players={players}
                        kickPlayer={(player: string) => send({
                            lobbyId: id,
                            type: "kickPlayer",
                            data: player
                        })}
                        owner={owner} />
                    <div className="flex flex-col gap-2">
                        <button className="w-full py-2 rounded-md bg-lime-500 text-black hover:bg-lime-400 active:bg-lime-600 transition"
                            onClick={() => send({ lobbyId: id, type: "readyPlayer" })}>
                            Ready Up
                        </button>

                        {owner === data?.username && <button className="w-full disabled:opacity-50 disabled:hover:bg-lime-500 disabled:active:bg-lime-500 py-2 rounded-md bg-lime-500 text-black hover:bg-lime-400 active:bg-lime-600 transition"
                            disabled={players.reduce<number>((acc, player) => {
                                return acc + (player.ready ? 1 : 0)
                            }, 0) != maxPlayers}
                            onClick={() => send({ lobbyId: id, type: "startGame" })}>
                            Start Game
                        </button>}
                    </div>
                </div>
            </div>
            <Chat messages={messages} sendChatMessage={(message: string) =>
                send({
                    lobbyId: id,
                    type: "lobbyChat",
                    data: message
                })} />
        </div>
    </div>
}

export default Lobby;
