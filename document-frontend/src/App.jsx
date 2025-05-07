import React, { useEffect, useState } from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import Home from './components/Home';
import SearchResults from './components/SearchResults';
import Login from './components/Login';
import ViewDocument from './components/ViewDocument';

const App = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const userData = sessionStorage.getItem("user");
        if (userData) {
            setUser(JSON.parse(userData));
        } else if (location.pathname !== '/login') {
            navigate('/login');
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
        <Routes>
            <Route path="/login" element={<Login onLogin={handleLogin} />} />
            {user && (
                <>
                    <Route path="/" element={<Home user={user} onLogout={handleLogout} />} />
                    <Route path="/search-results" element={<SearchResults user={user} />} />
                    <Route path="/view/:id" element={<ViewDocument />} />
                </>
            )}
        </Routes>
    );
};

export default App;
