import { useEffect, useRef, useState } from "react";

interface CircularCountdownProps {
    totalSeconds: number;       // total token duration (e.g., 86400 for 24h)
    remainingSeconds: number;   // current remaining seconds
    size?: number;              // diameter of the circle
    strokeWidth?: number;       // circle thickness
    status?: string;            // AUTH_CONFIRMED, RECONNECTING, etc.
}

const CircularCountdown: React.FC<CircularCountdownProps> = ({
    totalSeconds,
    remainingSeconds,
    size = 80,
    strokeWidth = 8,
    status = "AUTH_CONFIRMED",
}) => {
    const [animatedSeconds, setAnimatedSeconds] = useState(remainingSeconds);
    const requestRef = useRef<number>();

    const radius = (size - strokeWidth) / 2;
    const circumference = 2 * Math.PI * radius;

    // Smooth animation using requestAnimationFrame
    useEffect(() => {
        let startTime: number | null = null;
        const animate = (timestamp: number) => {
            if (!startTime) startTime = timestamp;
            // timestamp is used for animation frame tracking            // Animate remainingSeconds smoothly
            // Since remainingSeconds updates discreetly (every 1s), we interpolate
            const diff = remainingSeconds - animatedSeconds;

            // Simple linear interpolation for smoothness
            // Don't overshoot
            if (Math.abs(diff) < 0.1) {
                setAnimatedSeconds(remainingSeconds);
                return;
            }

            const step = diff * 0.1; // Ease in
            setAnimatedSeconds(prev => prev + step);

            requestRef.current = requestAnimationFrame(animate);
        };

        requestRef.current = requestAnimationFrame(animate);

        return () => {
            if (requestRef.current) cancelAnimationFrame(requestRef.current);
        };
    }, [remainingSeconds]);

    // Ensure totalSeconds is non-zero
    const safeTotal = totalSeconds > 0 ? totalSeconds : 1;
    const progressRatio = Math.max(0, Math.min(1, animatedSeconds / safeTotal));

    // Circle stroke offset
    const offset = circumference * (1 - progressRatio);

    // Dynamic color based on status
    let strokeColor = "#4caf50"; // green by default
    if (status === "EXPIRED" || remainingSeconds <= 0) strokeColor = "#f44336";      // red
    if (status === "RECONNECTING") strokeColor = "#ff9800"; // orange
    if (status === "UNAUTHENTICATED") strokeColor = "#9e9e9e"; // grey

    // Format time
    const formatTime = (secs: number) => {
        if (secs < 0) return "00:00";
        const h = Math.floor(secs / 3600);
        const m = Math.floor((secs % 3600) / 60);
        const s = Math.floor(secs % 60);

        if (h > 0) return `${h}h${m}m`;
        return `${m}:${s.toString().padStart(2, '0')}`;
    };

    return (
        <svg width={size} height={size} style={{ overflow: "visible" }}>
            {/* Background Circle */}
            <circle
                cx={size / 2}
                cy={size / 2}
                r={radius}
                stroke="#333"
                strokeWidth={strokeWidth}
                fill="none"
            />
            {/* Progress Circle */}
            <circle
                cx={size / 2}
                cy={size / 2}
                r={radius}
                stroke={strokeColor}
                strokeWidth={strokeWidth}
                fill="none"
                strokeDasharray={circumference}
                strokeDashoffset={offset}
                strokeLinecap="round"
                transform={`rotate(-90 ${size / 2} ${size / 2})`}
                style={{ transition: "stroke 0.5s ease" }}
            />
            <text
                x="50%"
                y="50%"
                dominantBaseline="middle"
                textAnchor="middle"
                fontSize={size * 0.2}
                fontFamily="monospace"
                fill="#ccc"
            >
                {formatTime(animatedSeconds)}
            </text>
        </svg>
    );
};

export default CircularCountdown;
