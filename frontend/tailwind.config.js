/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                terminal: {
                    bg: '#0a0a0a',
                    surface: '#141414',
                    border: '#262626',
                    text: '#e5e5e5',
                    muted: '#737373',
                    success: '#22c55e',
                    warning: '#f59e0b',
                    error: '#ef4444',
                    accent: '#3b82f6'
                }
            },
            fontFamily: {
                mono: ['JetBrains Mono', 'Consolas', 'monospace']
            }
        },
    },
    plugins: [],
}
