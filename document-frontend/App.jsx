// App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import DocumentDetail from './pages/DocumentDetail';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/documents/:id" element={<DocumentDetail />} />
            </Routes>
        </Router>
    );
}

export default App;