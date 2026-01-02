import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/v1/replay';

export const replayService = {
    loadJournal: async (path: string) => {
        try {
            await axios.post(`${API_BASE}/load`, { path });
            console.log('Journal loaded:', path);
        } catch (error) {
            console.error('Failed to load journal:', error);
        }
    },

    seek: async (timestamp: number) => {
        try {
            await axios.post(`${API_BASE}/seek/${timestamp}`);
            console.log('Seek successful:', timestamp);
        } catch (error) {
            console.error('Failed to seek:', error);
        }
    },

    play: async (startPtr: number, limit: number) => {
        try {
            await axios.post(`${API_BASE}/stream`, null, { params: { startPtr, limit } });
            console.log('Replay started');
        } catch (error) {
            console.error('Failed to play:', error);
        }
    }
};
