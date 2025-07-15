import { useMeQuery } from "@hooks/useMeQuery";
import { createContext, ReactNode, useCallback, useEffect, useRef, useState } from "react";

const websocketUrl = import.meta.env.PROD
    ? "wss://finar.skyegibney.com"
    : "";

type WebSocketContext = {
    send: (message: { gameId?: string, lobbyId?: string, type: string, data?: any }) => void,
    setMessageCallback: (messageCallback: (message: MessageEvent) => void) => void,
    connected: boolean
}

export const WebSocketContext = createContext<WebSocketContext>({
    send: () => { },
    setMessageCallback: () => { },
    connected: false
});

type WebSocketProviderProps = {
    children: ReactNode
}

export const WebSocketProvider = ({ children }: WebSocketProviderProps) => {
    const socketRef = useRef<WebSocket | null>(null);
    const {
        isLoading: authLoading,
        isError: authError
    } = useMeQuery();
    const [connected, setConnected] = useState(false);
    const [reconnecting, setReconnecting] = useState(false);

    const connect = useCallback(() => {
        if (socketRef.current === null && !reconnecting && !authLoading && !authError) {
            let intervalId = 0;

            const newSocket = new WebSocket(`${websocketUrl}/api/ws`);

            newSocket.onopen = () => {
                setInterval(() => {
                    newSocket.send(JSON.stringify({ type: "heartbeat" }));
                }, 2000);
                setConnected(true);
                setReconnecting(false);
            }

            newSocket.onclose = () => {
                setReconnecting(false);
                clearInterval(intervalId);
                setConnected(false);
                setReconnecting(true);
                socketRef.current = null;

                setTimeout(() => {
                    setReconnecting(false);
                    connect();
                }, 1000);
            }

            newSocket.onerror = (error) => {
                console.error(error);
            }

            socketRef.current = newSocket;
        }
    }, [authLoading, authError]);

    useEffect(() => {
        connect();

        return () => {
            socketRef.current?.close();
        };
    }, [authLoading, authError]);

    return (
        <WebSocketContext.Provider value={{
            send: (message) => {
                if (socketRef.current) {
                    socketRef.current.send(JSON.stringify(message));
                }
            },
            setMessageCallback: (messageCallback: (message: MessageEvent) => void) => {
                if (socketRef.current) {
                    socketRef.current.onmessage = messageCallback
                }
            },
            connected
        }}>
            {children}
        </WebSocketContext.Provider >
    )
}

export default WebSocketProvider;
