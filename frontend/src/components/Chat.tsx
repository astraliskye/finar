import { useState } from "react";

type ChatProps = {
    messages: {
        username: string,
        content: string
    }[];
    sendChatMessage: (message: string) => void;
}

function Chat({ messages, sendChatMessage }: ChatProps) {
    const [inputMessage, setInputMessage] = useState("");

    return <div className="h-96 min-w-96 max-w-3xl p-6 bg-stone-900 flex-grow rounded-lg flex flex-col gap-10">
        <div className="md:h-full flex-grow bg-stone-800 rounded-lg overflow-y-auto flex flex-col justify-end h-48">
            {messages.map((message, i) => (
                <p key={i} className={`${i % 2 == 0 ? "bg-stone-700" : "bg-stone-800"} px-4 py-2`}>
                    <span className="text-stone-400 text-sm mr-2">{message.username}</span> {message.content}
                </p>
            ))}
        </div>
        <form className="flex md:flex-row gap-4 flex-col"
            onSubmit={(e) => {
                e.preventDefault();

                if (inputMessage !== "") {
                    sendChatMessage(inputMessage);
                    setInputMessage("");
                }
            }}>
            <input type="text" placeholder="Type a message..."
                value={inputMessage}
                onChange={e => setInputMessage(e.target.value)}
                className="rounded-md px-2 py-1 text-black flex-grow" />
            <button
                className="bg-slate px-3 py-2 rounded-md bg-stone-800 hover:bg-stone-700 active:bg-stone-900 transition">
                Send
            </button>
        </form>
    </div>
}

export default Chat;
