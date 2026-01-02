import { Routes, Route } from 'react-router-dom'
import { Bootstrap } from './app/Bootstrap'
import { ProtectedRoute } from './app/ProtectedRoute'
import { GuardedLoginPage } from './guards/GuardedLoginPage'
import AuthCallback from './pages/AuthCallback'
import { PageProvider } from './context/PageContext'

// Terminal Layout
import TerminalLayout from './layouts/TerminalLayout'

// Pages (F1-F8)
import Dashboard from './pages/Dashboard'
import MarketWatch from './pages/MarketWatch'
import OptionsChain from './pages/OptionsChain'
import Sectors from './pages/Sectors'
import Orders from './pages/Orders'
import RiskDashboard from './pages/RiskDashboard'
import AccountPage from './pages/AccountPage'
import SettingsPage from './pages/SettingsPage'
import VegaDashboard from './pages/VegaDashboard'

import { useKillSwitch } from './hooks/useKillSwitch'

import { AuthProvider } from './context/AuthProvider'

/**
 * Main App Component - Bloomberg-style Terminal.
 */
function App() {
    useKillSwitch()
    // useCommandPalette call if defined

    return (
        <PageProvider>
            <AuthProvider>
                <Bootstrap>
                    <Routes>
                        <Route path="/login" element={<GuardedLoginPage />} />
                        <Route path="/auth/callback" element={<AuthCallback />} />

                        <Route
                            path="/*"
                            element={
                                <ProtectedRoute>
                                    <TerminalLayout />
                                </ProtectedRoute>
                            }
                        >
                            <Route index element={<Dashboard />} />
                            <Route path="dashboard" element={<Dashboard />} />
                            <Route path="market-watch" element={<MarketWatch />} />
                            <Route path="options" element={<OptionsChain />} />
                            <Route path="sectors" element={<Sectors />} />
                            <Route path="orders" element={<Orders />} />
                            <Route path="risk" element={<RiskDashboard />} />
                            <Route path="account" element={<AccountPage />} />
                            <Route path="settings" element={<SettingsPage />} />
                            <Route path="vega-status" element={<VegaDashboard />} />
                        </Route>
                    </Routes>
                </Bootstrap>
            </AuthProvider>
        </PageProvider>
    )
}

export default App;
