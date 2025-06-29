import React, { useEffect, useState } from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import Home from './components/Home';
import SearchResults from './components/SearchResults';
import Login from './components/Login';
import ViewDocument from './components/ViewDocument';
import Footer from './components/Footer';
import axios from "axios";
import API_BASE_URL from './components/api';

const App = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const localUser = sessionStorage.getItem("user");
        if (localUser) {
            setUser(JSON.parse(localUser));
        } else {
            axios.get(`${API_BASE_URL}/auth/status`, { withCredentials: true })
                .then(res => {
                    setUser(res.data);
                    sessionStorage.setItem("user", JSON.stringify(res.data));
                })
                .catch(() => {
                    setUser(null);
                    if (location.pathname !== '/login') {
                        navigate('/login');
                    }
                });
        }
    }, [navigate, location.pathname]);



    const handleLogout = () => {
        sessionStorage.removeItem("user");
        setUser(null);
        navigate('/login');
    };

    const handleLogin = (userData) => {
        sessionStorage.setItem("user", JSON.stringify(userData));
        setUser(userData);
        navigate('/');
    };

    return (
        <>

        <Routes>
            <Route path="/login" element={<Login onLogin={handleLogin} />} />
            <Route path="/" element={<Home onLogout={handleLogout} user={user} />} />
            <Route path="/search-results" element={<SearchResults user={user} />} />
            <Route path="/view/:id" element={<ViewDocument />} />
        </Routes>
        <Footer />
        </>
    );
};

export default App;
