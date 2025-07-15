import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import Home from '@features/home/Home.tsx'
import Login from '@features/auth/Login.tsx'
import Register from '@features/auth/Register.tsx'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import Play from '@features/play/Play.tsx'
import Lobby from '@features/matchmaking/Lobby.tsx'
import WebSocketProvider from '@contexts/WebSocketContext.tsx'
import HeaderLayout from './layouts/HeaderLayout'

const router = createBrowserRouter([
    {
        path: "/",
        element: <HeaderLayout>
            <Home />
        </HeaderLayout>
    },
    {
        path: "/game/:id",
        element: <HeaderLayout>
            <Play />
        </HeaderLayout>
    },
    {
        path: "/lobby/:id",
        element: <HeaderLayout>
            <Lobby />
        </HeaderLayout>
    },
    {
        path: "/login",
        element: <Login />
    },
    {
        path: "/register",
        element: <Register />
    },
    {
        path: "*",
        element: <Home />
    }
])

const queryClient = new QueryClient();

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <QueryClientProvider client={queryClient}>
            <WebSocketProvider>
                <RouterProvider router={router} />
            </WebSocketProvider>
        </QueryClientProvider>
    </React.StrictMode>,
)
