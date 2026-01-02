import { PropsWithChildren } from 'react';
import { AuthContext } from './AuthContext';
import { useAuthStatus } from '../hooks/useAuthStatus';

export function AuthProvider({ children }: PropsWithChildren) {
    // This hook is now guaranteed to return a non-null object
    const authState = useAuthStatus();

    return (
        <AuthContext.Provider value={authState}>
            {children}
        </AuthContext.Provider>
    );
}
