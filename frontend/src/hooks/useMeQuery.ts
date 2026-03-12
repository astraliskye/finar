import { useQuery } from "@tanstack/react-query";

export function useMeQuery() {
    const meQuery = useQuery<{
        id: number,
        username: string,
        email: string,
        roles: string[]
    }>({
        queryKey: ["userData"],
        queryFn: async () => {
            const response = await fetch("/api/me");
            const body = await response.json();

            if (response.status !== 200) {
                throw new Error(body.error);
            }

            return body;
        },
        retry: false,
        refetchOnMount: false,
        refetchOnReconnect: false,
        refetchOnWindowFocus: false,
        staleTime: 10 * 60 * 1000
    });

    return meQuery;
}
