import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errorText, setErrorText] = useState("");
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const queryClient = useQueryClient();

    const loginMutation = useMutation({
        mutationFn: async ({ user, pass }: { user: string, pass: string }) => {
            const response = await fetch("/api/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ username: user, password: pass })
            });

            if (response.status !== 200) {
                const errorData = await response.json();
                console.log(errorData);
                throw new Error(errorData.error)
            }
        },
        onSuccess: () => {
            queryClient.refetchQueries({
                queryKey: ["userData"],
            });

            navigate(searchParams.get("redirectTo") || "/", {
                replace: true,
            });
        },
        onError: (error) => {
            setErrorText(error.message);
        }
    });

    return (
        <form onSubmit={(e) => {
            e.preventDefault();
            setErrorText("");
            loginMutation.mutate({ user: username, pass: password })
        }}
            className="w-screen h-screen flex flex-col items-center justify-center gap-4"
        >
            <p className="text-red-600 h-8">{errorText}</p>
            <input type="text" value={username}
                required
                placeholder="Username"
                onChange={(e) => setUsername(e.target.value)}
                className="border-2 border-gray-600 rounded-md px-2 py-1 bg-transparent"
                autoFocus
            />
            <input type="password" value={password}
                required
                placeholder="Password"
                onChange={(e) => setPassword(e.target.value)}
                className="border-2 border-gray-600 rounded-md px-2 py-1 bg-transparent"
            />
            <button
                className="bg-primary rounded-md px-2 py-1 text-black"
            >
                Login
            </button>
            <Link
                to="/register"
                className="text-sm underline"
            >
                Don't have an account? Register for one
            </Link>
        </form>
    );
}

export default Login;
