import { useState, useEffect } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { bootAuth, getAuthState, subscribeToAuthChanges, AuthState } from './authSupervisor';

/**
 * AuthBoundary - Single Router-Level Auth Guard
 * 
 * This is the ONLY auth guard in the entire application.
 * Everything inside this boundary is guaranteed authenticated.
 * 
 * Properties:
 * - Runs exactly ONCE per tab load
 * - StrictMode safe (uses singleton bootAuth)
 * - Multi-tab safe (syncs via BroadcastChannel)
 * - No per-component session checks
 */
export function AuthBoundary() {
    const [state, setState] = useState<AuthState>(() => getAuthState());

    useEffect(() => {
        // If still booting, trigger the singleton session check
        if (state.status === 'booting') {
            console.log('[AuthBoundary] Triggering bootAuth...');
            bootAuth().then(newState => {
                console.log('[AuthBoundary] bootAuth resolved:', newState.status);
                setState(newState);
            });
        }

        // Subscribe to auth changes from other tabs
        const unsubscribe = subscribeToAuthChanges((newState) => {
            console.log('[AuthBoundary] Auth state changed from another tab:', newState.status);
            setState(newState);
        });

        return unsubscribe;
    }, [state.status]);

    // Booting - show splash screen
    if (state.status === 'booting') {
        return (
            <div className="min-h-screen flex items-center justify-center bg-terminal-bg">
                <div className="text-center">
                    <div className="text-terminal-success text-lg font-bold mb-2">
                        VEGA TRADER
                    </div>
                    <div className="text-terminal-muted text-sm">
                        Establishing secure uplink...
                    </div>
                </div>
            </div>
        );
    }

    // Unauthenticated - redirect to login
    if (state.status === 'unauthenticated') {
        console.log('[AuthBoundary] Unauthenticated - redirecting to /login');
        return <Navigate to="/login" replace />;
    }

    // Authenticated - render protected content
    console.log('[AuthBoundary] ✓ Authenticated - rendering protected content');
    return <Outlet />;
}

export default AuthBoundary;
