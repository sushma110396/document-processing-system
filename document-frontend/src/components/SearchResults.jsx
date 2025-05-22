import React, { useEffect, useState } from "react";
import axios from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import './css/SearchResults.css';

const SearchResults = ({ user }) => {
    const location = useLocation();
    const navigate = useNavigate();
    const query = new URLSearchParams(location.search).get('q');

    const [results, setResults] = useState([]);
    const [status, setStatus] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchResults = async () => {
            if (!query) return;

            setLoading(true);
            setStatus("");

            try {
                const response = await axios.get("http://localhost:9090/documents/search", {
                    params: { q: query, userId: user.userId },
                });

                if (Array.isArray(response.data)) {
                    setResults(response.data);
                    setStatus(`${response.data.length} result(s) found.`);
                } else {
                    setResults([]);
                    setStatus("No documents found.");
                }
            } catch (error) {
                console.error("Search failed:", error);
                setStatus("An error occurred while searching.");
            } finally {
                setLoading(false);
            }
        };

        fetchResults();
    }, [query, user.userId]);

    return (
        <div className="search-results-container">
            <h2>Search Results for "{query}"</h2>

            {loading ? (
                <p className="status-text">Loading search results...</p>
            ) : (
                <>
                    <p className="status-text">{status}</p>
                    <ul className="search-result-list">
                        {results.map((result) => (
                            <li key={result.id}>
                                <strong>{result.name}</strong>
                                <br />
                                {result.preview?.substring(0, 100)}...
                            </li>
                        ))}
                    </ul>
                </>
            )}

            <button onClick={() => navigate('/')} className="back-button">Back to Home</button>
        </div>

    );
};

export default SearchResults;
