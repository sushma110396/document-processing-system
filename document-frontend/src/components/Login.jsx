import React, { useState } from "react";
import axios from "axios";
import './css/Login.css';

const Login = ({ onLogin }) => {
    const [isLoginMode, setIsLoginMode] = useState(true);
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState(""); // Only used during registration
    const [password, setPassword] = useState("");

    const handleLogin = async () => {
        try {
            const response = await axios.post("http://localhost:9090/auth/login", {
                username,
                password,
            });
            alert("Login successful");
            onLogin(response.data); // Pass user info to parent
        } catch (err) {
            alert("Invalid username or password");
        }
    };

    const handleRegister = async () => {
        try {
            await axios.post("http://localhost:9090/auth/register", {
                username,
                email,
                password,
            });
            alert("Registration successful. Please log in.");
            setIsLoginMode(true); // Switch to login view
        } catch (err) {
            alert("Registration failed: " + (err.response?.data || "Unknown error"));
        }
    };

    return (
        <div className="login">
            <h2 className="login-mode">{isLoginMode ? "Login" : "Register"}</h2>
            <input type="text" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} className="username"/>
            {!isLoginMode && (
                <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} className="email"/>
            )}
            <div className="form-group">
                <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} className="password"/>
                {isLoginMode ? (
                    <button onClick={handleLogin}>Login</button>
                ) : (
                    <button onClick={handleRegister}>Register</button>
                )}
            </div>
            <br />
            <button id="login" onClick={() => setIsLoginMode(!isLoginMode)}>
                {isLoginMode ? "Need an account? Register" : "Have an account? Login"}
            </button>
        </div>
    );
};

export default Login;
