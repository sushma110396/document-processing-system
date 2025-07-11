import React, { useState } from "react";
import axios from "axios";
import './css/Login.css';
import API_BASE_URL from './api';

const Login = ({ onLogin }) => {
    const [isLoginMode, setIsLoginMode] = useState(true);
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [toastMessage, setToastMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async () => {
        setLoading(true);
        try {
            const response = await axios.post(
                `${API_BASE_URL}/auth/login`,
                {
                    username,
                    password,
                },
                { withCredentials: true }
            );

            sessionStorage.setItem("user", JSON.stringify(response.data));
            setToastMessage("Login successful");

            setTimeout(() => {
                setToastMessage("");
                onLogin(response.data);
            }, 4000);

        } catch (err) {
            console.error("Login error:", err); //Add debug info
            setToastMessage("Invalid username or password");
            setTimeout(() => setToastMessage(""), 3000);
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async () => {
        setLoading(true);
        try {
            await axios.post("https://document-processing-system.onrender.com/auth/register", {
                username,
                email,
                password,
            }, {
                withCredentials: true
            });

            setToastMessage("Registration successful. Please log in.");
            setIsLoginMode(true); // Switch to login view
        } catch (err) {
            alert("Registration failed: " + (err.response?.data || "Unknown error"));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-wrapper">
            {loading && (
                <p className="loading-message">
                    {isLoginMode ? "Logging in... Please wait." : "Registering... Please wait."}
                </p>
            )}
        <div className="login">
            {toastMessage && (
                <div className="toast">
                    <span className="icon">&#10004;</span>
                    <span>{toastMessage}</span>
                </div>
            )}
            <h2 className="login-mode">{isLoginMode ? "Login" : "Register"}</h2>
            <input type="text" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} className="username" />
            {!isLoginMode && (
                <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} className="email" />
            )}
            <div className="form-group">
                <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} className="password" />
                {isLoginMode ? (
                    <button onClick={handleLogin}>Login</button>
                ) : (
                    <button id="register" onClick={handleRegister}>Register</button>
                )}
            </div>
            <br />
            <button id="login" onClick={() => setIsLoginMode(!isLoginMode)}>
                {isLoginMode ? "Need an account? Register" : "Have an account? Login"}
            </button>
            </div>
        </div>
    );

};

export default Login;
