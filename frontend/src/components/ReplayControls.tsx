import { useState } from 'react';

interface ReplayControlsProps {
    onLoad: (path: string) => void;
    onSeek: (timestamp: number) => void;
    onPlay: (startPtr: number, limit: number) => void;
}

export const ReplayControls: React.FC<ReplayControlsProps> = ({ onLoad, onSeek, onPlay }) => {
    const [path, setPath] = useState('');
    const [timestamp, setTimestamp] = useState<number>(Date.now());
    const limit = 100;

    return (
        <div className="p-4 bg-gray-800 text-white rounded-lg shadow-md">
            <h3 className="text-lg font-bold mb-4">Market Replay</h3>

            <div className="mb-4">
                <label className="block text-sm text-gray-400">Journal Path</label>
                <div className="flex gap-2">
                    <input
                        type="text"
                        value={path}
                        onChange={(e) => setPath(e.target.value)}
                        className="flex-1 bg-gray-700 p-2 rounded text-sm"
                        placeholder="/path/to/journal.dat"
                    />
                    <button
                        onClick={() => onLoad(path)}
                        className="px-4 py-2 bg-blue-600 hover:bg-blue-500 rounded text-sm font-semibold"
                    >
                        Load
                    </button>
                </div>
            </div>

            <div className="mb-4">
                <label className="block text-sm text-gray-400">Seek Timestamp (Epoch)</label>
                <div className="flex gap-2">
                    <input
                        type="number"
                        value={timestamp}
                        onChange={(e) => setTimestamp(Number(e.target.value))}
                        className="flex-1 bg-gray-700 p-2 rounded text-sm"
                    />
                    <button
                        onClick={() => onSeek(timestamp)}
                        className="px-4 py-2 bg-yellow-600 hover:bg-yellow-500 rounded text-sm font-semibold"
                    >
                        Seek
                    </button>
                </div>
            </div>

            <div>
                <button
                    onClick={() => onPlay(0, limit)}
                    className="w-full px-4 py-2 bg-green-600 hover:bg-green-500 rounded text-sm font-semibold"
                >
                    Stream Replay
                </button>
            </div>
        </div>
    );
};
