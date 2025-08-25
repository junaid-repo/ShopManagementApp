// src/App.js
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate, useNavigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import MainLayout from './components/MainLayout';
import DashboardPage from './pages/DashboardPage';
import ProductsPage from './pages/ProductsPage';
import SalesPage from './pages/SalesPage';
import CustomersPage from './pages/CustomersPage';
import PaymentsPage from './pages/PaymentsPage';
import BillingPage from './pages/BillingPage';
import ReportsPage from './pages/ReportsPage';
import UserProfilePage from './pages/UserProfilePage';
import AnalyticsPage from './pages/AnalyticsPage';

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    // 🔹 Check token in localStorage when app loads
    useEffect(() => {
        const token = localStorage.getItem('jwt_token');
        if (token) {
            setIsAuthenticated(true);
        }
    }, []);
useEffect(() => {
    const checkAuth = () => {
        const token = localStorage.getItem('jwt_token');
        setIsAuthenticated(!!token); // true if token exists, false if not
    };

    // Run once on mount
    checkAuth();

    // Listen for changes in localStorage (logout in another tab, token expired, etc.)
    window.addEventListener("storage", checkAuth);

    return () => {
        window.removeEventListener("storage", checkAuth);
    };
}, []);
    const handleLogin = () => {
        setIsAuthenticated(true);
    };

    const handleLogout = () => {
        localStorage.removeItem('jwt_token'); // clear token
        setIsAuthenticated(false);
    };

    return (
        <Router>
            <Routes>
                {/* Login route */}
                <Route
                    path="/login"
                    element={
                        !isAuthenticated
                            ? <LoginPage onLogin={handleLogin} />
                            : <Navigate to="/" replace />
                    }
                />

                {/* Protected routes */}
                <Route
                    path="/*"
                    element={
                        isAuthenticated
                            ? <ProtectedRoutes onLogout={handleLogout} />
                            : <Navigate to="/login" replace />
                    }
                />
            </Routes>
        </Router>
    );
}

const ProtectedRoutes = ({ onLogout }) => {
    const navigate = useNavigate();

    // ✅ handleLogout wrapper that also redirects
    const logoutAndRedirect = () => {
        onLogout();
        navigate("/login", { replace: true });
    };

    return (
        <MainLayout onLogout={logoutAndRedirect}>
            <Routes>
                <Route path="/" element={<DashboardPage />} />
                <Route path="/products" element={<ProductsPage />} />
                <Route path="/sales" element={<SalesPage />} />
                <Route path="/customers" element={<CustomersPage />} />
                <Route path="/payments" element={<PaymentsPage />} />
                <Route path="/billing" element={<BillingPage />} />
                <Route path="/reports" element={<ReportsPage />} />
                <Route path="/profile" element={<UserProfilePage />} />
                <Route path="/analytics" element={<AnalyticsPage />} />
            </Routes>
        </MainLayout>
    );
};

export default App;
