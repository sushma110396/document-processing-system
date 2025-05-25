import React, { useState } from "react";
import axios from "axios";
import './css/Login.css';

const Login = ({ onLogin }) => {
    const [isLoginMode, setIsLoginMode] = useState(true);
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [toastMessage, setToastMessage] = useState("");


    const handleLogin = async () => {
        try {
            const response = await axios.post("http://document-processing-system.onrender.com/auth/login", {
                username,
                password,
            });

            sessionStorage.setItem("user", JSON.stringify(response.data));
            setToastMessage("Login successful");

            // Delay navigation so toast can be seen
            setTimeout(() => {
                setToastMessage("");
                onLogin(response.data); 
            }, 4000);

        } catch (err) {
            setToastMessage("Invalid username or password");
            setTimeout(() => setToastMessage(""), 3000);
        }
    };


    const handleRegister = async () => {
        try {
            await axios.post("http://document-processing-system.onrender.com/auth/register", {
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
