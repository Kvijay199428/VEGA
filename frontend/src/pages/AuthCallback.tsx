import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import './AuthCallback.css';

interface CallbackState {
    status: 'PROCESSING' | 'SUCCESS' | 'ERROR';
    message: string;
    details: Record<string, string>;
    timestamp: string;
}

export default function AuthCallback() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [state, setState] = useState<CallbackState>({
        status: 'PROCESSING',
        message: 'PROCESSING AUTHORIZATION CODE...',
        details: {},
        timestamp: new Date().toISOString()
    });

    useEffect(() => {
        const code = searchParams.get('code');
        const error = searchParams.get('error');
        const errorDescription = searchParams.get('error_description');

        if (error) {
            setState({
                status: 'ERROR',
                message: `AUTHORIZATION FAILED: ${error.toUpperCase()}`,
                details: {
                    'ERROR_CODE': error,
                    'DESCRIPTION': errorDescription || 'Unknown error',
                    'TIMESTAMP': new Date().toISOString()
                },
                timestamp: new Date().toISOString()
            });
            return;
        }

        if (code) {
            setState({
                status: 'SUCCESS',
                message: 'AUTHORIZATION CODE RECEIVED',
                details: {
                    'AUTH_CODE': code.substring(0, 20) + '...',
                    'STATUS': 'TOKEN EXCHANGE IN PROGRESS',
                    'TIMESTAMP': new Date().toISOString()
                },
                timestamp: new Date().toISOString()
            });

            // Auto-redirect to dashboard after 3 seconds
            setTimeout(() => {
                navigate('/dashboard');
            }, 3000);
        }
    }, [searchParams, navigate]);

    const getStatusColor = () => {
        switch (state.status) {
            case 'SUCCESS': return '#00ff00';
            case 'ERROR': return '#ff4444';
            default: return '#ffaa00';
        }
    };

    return (
        <div className="bloomberg-terminal">
            <div className="terminal-header">
                <div className="header-left">
                    <span className="header-title">VEGA TRADER</span>
                    <span className="header-subtitle">AUTHENTICATION GATEWAY</span>
                </div>
                <div className="header-right">
                    <span className="timestamp">{state.timestamp}</span>
                </div>
            </div>

            <div className="terminal-body">
                <div className="status-panel" style={{ borderColor: getStatusColor() }}>
                    <div className="status-indicator" style={{ backgroundColor: getStatusColor() }}>
                        {state.status}
                    </div>
                    <div className="status-message">{state.message}</div>
                </div>

                <div className="data-grid">
                    <div className="grid-header">
                        <span className="grid-title">AUTHORIZATION DETAILS</span>
                    </div>
                    <div className="grid-body">
                        {Object.entries(state.details).map(([key, value]) => (
                            <div key={key} className="grid-row">
                                <span className="grid-key">{key}</span>
                                <span className="grid-value">{value}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {state.status === 'SUCCESS' && (
                    <div className="redirect-notice">
                        <span className="blink">▶</span> REDIRECTING TO DASHBOARD IN 3 SECONDS...
                    </div>
                )}

                {state.status === 'ERROR' && (
                    <div className="action-panel">
                        <button className="terminal-button" onClick={() => navigate('/login')}>
                            [ RETRY LOGIN ]
                        </button>
                    </div>
                )}
            </div>

            <div className="terminal-footer">
                <div className="footer-left">
                    <span>UPSTOX API V2</span>
                    <span className="separator">|</span>
                    <span>OAUTH 2.0</span>
                </div>
                <div className="footer-right">
                    <span>PORT: 28020</span>
                    <span className="separator">|</span>
                    <span>SECURE</span>
                </div>
            </div>
        </div>
    );
}
