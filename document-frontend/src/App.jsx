import React, { useState } from 'react';
import Home from './components/Home';
import Login from './components/Login';

function App() {
    const [user, setUser] = useState(null);

    const handleLogout = () => {
        setUser(null); 
    };

    return user ? (
        <Home user={user} onLogout={handleLogout} />
    ) : (
        <Login onLogin={setUser} />
    );
}

export default App;
