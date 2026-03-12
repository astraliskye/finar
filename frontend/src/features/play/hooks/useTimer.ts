import { useEffect, useState } from "react";

function useTimer(initialTime: number) {
    const [time, setTime] = useState<number>(initialTime);
    const [referenceInstant, setReferenceInstant] = useState<number>(Date.now());
    const [timerTotal, setTimerTotal] = useState<number>(initialTime);
    const [isRunning, setIsRunning] = useState(false);

    useEffect(() => {
        let intervalId: NodeJS.Timeout;

        if (isRunning) {
            if (time > 10000) {
                intervalId = setInterval(() => {
                    setTime(timerTotal - (Date.now() - referenceInstant));

                    if (time < 0) {
                        setTime(0);
                        setIsRunning(false);
                    }
                }, 400);
            } else {
                intervalId = setInterval(() => {
                    setTime(timerTotal - (Date.now() - referenceInstant));

                    if (time < 0) {
                        setTime(0);
                        setIsRunning(false);
                    }
                }, 10);
            }
        }

        return () => clearInterval(intervalId);
    }, [isRunning, time])

    const start = () => setIsRunning(true);
    const stop = () => setIsRunning(false);
    const set = (time: number) => {
        setTime(time);
        setTimerTotal(time);
        setReferenceInstant(Date.now());
    }

    return {start, stop, set, isRunning, time};
}

export default useTimer;
