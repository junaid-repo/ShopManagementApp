// src/pages/LoginPage.js
import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';
import { useConfig } from "./ConfigProvider";



const LoginPage = ({ onLogin }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const inactivityTimerRef = useRef(null);


    // Clears and sets a new inactivity timer
    const resetInactivityTimer = () => {
        if (inactivityTimerRef.current) clearTimeout(inactivityTimerRef.current);
        inactivityTimerRef.current = setTimeout(() => {
            handleLogout();
        }, 15 * 60 * 1000); // 15 minutes
    };

    const handleLogout = () => {
        localStorage.removeItem('jwt_token');
        window.removeEventListener('mousemove', resetInactivityTimer);
        window.removeEventListener('keydown', resetInactivityTimer);
        onLogin(false); // tell parent user is logged out
        navigate('/login');
    };

const config = useConfig();
var apiUrl="";
  if(config){
  console.log(config.API_URL);
  apiUrl=config.API_URL;
  }
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await fetch(apiUrl+"/auth/authenticate", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                throw new Error('Login failed');
            }


          const data = await response.text();
         // localStorage.setItem('jwt_token', data);
           // const data = await response.json();
            console.log('Login response:', data);
            if (data) {
                localStorage.setItem('jwt_token', data);
                onLogin(true);

                // Set idle timeout listeners
                window.addEventListener('mousemove', resetInactivityTimer);
                window.addEventListener('keydown', resetInactivityTimer);
                resetInactivityTimer();

                navigate('/');
            } else {
                setError(data.message || 'Invalid username or password');
            }
        } catch (err) {
            setError(err.message || 'An error occurred during login');
        }
    };

    // Clean up listeners when unmounting
    useEffect(() => {
        return () => {
            clearTimeout(inactivityTimerRef.current);
            window.removeEventListener('mousemove', resetInactivityTimer);
            window.removeEventListener('keydown', resetInactivityTimer);
        };
    }, []);

    return (
        <div className="login-container">
            <div className="login-box glass-card">
                <h1 className="login-logo">ShopFlow</h1>
                <h2>Admin Login</h2>
                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="input-group">
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    {error && <p className="error-message">{error}</p>}
                    <button type="submit" className="btn login-btn">Login</button>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;
