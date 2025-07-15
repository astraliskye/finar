import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useMeQuery } from "@hooks/useMeQuery";
import { ReactNode } from "react";
import { Link } from "react-router-dom";

type HeaderLayoutProps = {
    children: ReactNode
}

function HeaderLayout({ children }: HeaderLayoutProps) {
    const { data, isError } = useMeQuery();
    const queryClient = useQueryClient();

    const logoutMutation = useMutation({
        mutationFn: async () => {
            const response = await fetch("/api/logout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
            });

            if (response.status !== 200) {
                const errorData = await response.json();
                throw new Error(errorData.error);
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: ["userData"]
            })
        }
    });

    return <>
        <header className="fixed top-0 flex justify-between items-center px-4 w-screen border-b-2 border-stone-500 h-16 bg-stone-800">
            <Link to="/" className="text-primary font-bold tracking-widest select-none">finar</Link>
            {isError || !data
                ? <div className="flex gap-4">
                    <Link
                        to="/register"
                        className="select-none px-2 py-1 bg-primary text-black rounded-md"
                    >
                        Register
                    </Link>
                    <Link
                        to="/login"
                        className="select-none px-2 py-1 underline rounded-md"
                    >
                        Log In
                    </Link>
                </div>
                :
                <div className="flex items-center justify-center gap-4">
                    <p>Logged in as <span className="text-primary">{data.username}</span></p>
                    <button
                        className="px-2 py-1 underline"
                        onClick={() => {
                            logoutMutation.mutate()
                        }}
                    >
                        Log Out
                    </button>
                </div>
            }
        </header>
        <main className="pt-16">
            {children}
        </main>
    </>
}

export default HeaderLayout;
