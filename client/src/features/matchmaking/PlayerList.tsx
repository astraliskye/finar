import { useMeQuery } from "@hooks/useMeQuery"

type PlayerListProps = {
    players: {
        username: string,
        lobbyOwner: boolean,
        ready: boolean
    }[],
    kickPlayer: (player: string) => void,
    owner: string
}

function PlayerList({ players, kickPlayer, owner }: PlayerListProps) {
    const { data } = useMeQuery();

    return <div className="flex flex-col gap-2">
        <p className="text-center text-lg">Players</p>
        {players.map(player => (
            <div key={player.username} className="bg-stone-800 rounded-md p-2 flex items-center justify-between">
                <div className="flex gap-2 items-center">
                    <svg className={player.lobbyOwner ? "" : "invisible"} height="15px" width="15px" version="1.1" id="Capa_1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 47.94 47.94" fill="gold"><g id="SVGRepo_bgCarrier" strokeWidth="0"></g><g id="SVGRepo_tracerCarrier" strokeLinecap="round" strokeLinejoin="round"></g><g id="SVGRepo_iconCarrier"> <path d="M26.285,2.486l5.407,10.956c0.376,0.762,1.103,1.29,1.944,1.412l12.091,1.757 c2.118,0.308,2.963,2.91,1.431,4.403l-8.749,8.528c-0.608,0.593-0.886,1.448-0.742,2.285l2.065,12.042 c0.362,2.109-1.852,3.717-3.746,2.722l-10.814-5.685c-0.752-0.395-1.651-0.395-2.403,0l-10.814,5.685 c-1.894,0.996-4.108-0.613-3.746-2.722l2.065-12.042c0.144-0.837-0.134-1.692-0.742-2.285l-8.749-8.528 c-1.532-1.494-0.687-4.096,1.431-4.403l12.091-1.757c0.841-0.122,1.568-0.65,1.944-1.412l5.407-10.956 C22.602,0.567,25.338,0.567,26.285,2.486z"></path> </g></svg>
                    <p>
                        {player.username}
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <svg className={player.ready ? "" : "invisible"} width="20px" height="20px" fill="lime" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M18.047,4,22,8.325,9.3,20,2,12.68,6.136,8.533,9.474,11.88Z" /></svg>
                    {data?.username === owner && player.username !== data?.username
                        && <svg xmlns="http://www.w3.org/2000/svg" width="20" viewBox="0 0 24 24"
                            className="cursor-pointer"
                            onClick={() => kickPlayer(player.username)}>
                            <path
                                fill="red"
                                d="M18.3 5.7c-.4-.4-1-.4-1.4 0L12 10.6 7.1 5.7c-.4-.4-1-.4-1.4 0-.4.4-.4 1 0 1.4L10.6 12l-4.9 4.9c-.4.4-.4 1 0 1.4.2.2.4.3.7.3.3 0 .5-.1.7-.3L12 13.4l4.9 4.9c.2.2.4.3.7.3.3 0 .5-.1.7-.3.4-.4.4-1 0-1.4L13.4 12l4.9-4.9c.4-.4.4-1 0-1.4z"
                            />
                        </svg>
                    }
                </div>
            </div>
        ))
        }
    </div >
}

export default PlayerList;
