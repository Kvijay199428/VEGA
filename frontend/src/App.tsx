import { Routes, Route } from 'react-router-dom'
import { Bootstrap } from './app/Bootstrap'
import { ProtectedRoute } from './app/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import AuthCallback from './pages/AuthCallback'

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

/**
 * Main App Component - Bloomberg-style Terminal.
 * 
 * Structure:
 * - Bootstrap wraps everything for session recovery
 * - /login is public
 * - /auth/callback is public (OAuth redirect)
 * - All other routes use TerminalLayout with F1-F8 navigation
 */
function App() {
    return (
        <Bootstrap>
            <Routes>
                {/* Public routes */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/auth/callback" element={<AuthCallback />} />

                {/* Protected routes with Terminal Layout */}
                <Route
                    path="/*"
                    element={
                        <ProtectedRoute>
                            <TerminalLayout />
                        </ProtectedRoute>
                    }
                >
                    {/* F1: Dashboard (default) */}
                    <Route index element={<Dashboard />} />
                    <Route path="dashboard" element={<Dashboard />} />

                    {/* F2: Market Watch */}
                    <Route path="market-watch" element={<MarketWatch />} />

                    {/* F3: Options Chain */}
                    <Route path="options" element={<OptionsChain />} />

                    {/* F4: Sectors */}
                    <Route path="sectors" element={<Sectors />} />

                    {/* F5: Orders */}
                    <Route path="orders" element={<Orders />} />

                    {/* F6: Risk Dashboard */}
                    <Route path="risk" element={<RiskDashboard />} />

                    {/* F7: Account */}
                    <Route path="account" element={<AccountPage />} />

                    {/* F8: Settings */}
                    <Route path="settings" element={<SettingsPage />} />
                </Route>
            </Routes>
        </Bootstrap>
    )
}

export default App
