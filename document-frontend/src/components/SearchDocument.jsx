import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import './css/SearchDocument.css';

const SearchDocument = ({ user }) => {
    const [query, setQuery] = useState("");
    const [status, setStatus] = useState("");
    const navigate = useNavigate();

    const handleSearch = (e) => {
        e.preventDefault();

        if (!query.trim()) {
            setStatus("Please enter a search term.");
            return;
        }

        // Navigate to search-results page with query string
        navigate(`/search-results?q=${encodeURIComponent(query.trim())}`);
    };

    const handleClear = () => {
        setQuery('');
        setStatus('');
    };

    return (
        <div className="search-documents">
            <form onSubmit={handleSearch} className="search-form">
                <div className="search-bar-wrapper">
                    <input
                        type="text"
                        placeholder="Enter keyword to search for a document..."
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        className="search-input"
                    />
                    <button id="search" type="submit">Search</button>
                </div>
            </form>

            <p className="status-text">{status}</p>
        </div>
    );
};

export default SearchDocument;
