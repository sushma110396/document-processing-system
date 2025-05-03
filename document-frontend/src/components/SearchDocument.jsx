import React, { useState } from "react";
import axios from "axios";
import './css/SearchDocument.css';

const SearchDocument = ({ user }) => {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState([]);
    const [status, setStatus] = useState("");

    const handleSearch = async (e) => {
        e.preventDefault();
        if (!query.trim()) {
            setStatus("Please enter a search term.");
            return;
        }

        try {
            const response = await axios.get("http://localhost:9090/documents/search", {
                params: { q: query, userId: user.userId },
            });

            console.log("Search results:", response.data);

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
        }
    };

    const handleClear = () => {
        setQuery('');
        setResults([]);
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
                    <button type="button" id="clear" onClick={handleClear}>Clear</button>
                </div>
            </form>

            <p className="status-text">{status}</p>

            <div className="search-results-container">
                <ul className="search-result">
                    {results.map((result) => (
                        <li key={result.id}>
                            <strong>{result.document?.name}</strong> ({result.document?.type})
                            <br />
                            <em>Preview:</em> {result.extractedText?.substring(0, 100)}...
                        </li>
                    ))}
                </ul>
            </div>
        </div>

    );
};

export default SearchDocument;
