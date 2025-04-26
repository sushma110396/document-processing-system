import React, { useState } from "react";
import axios from "axios";

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

    return (
        <div>
            <h2>Search Documents</h2>
            <form onSubmit={handleSearch}>
                <input
                    type="text"
                    placeholder="Enter keyword"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                />
                <button type="submit">Search</button>
            </form>
            <p>{status}</p>
            <ul>
                {results.map((result) => (
                    <li key={result.id}>
                        <strong>{result.document?.name}</strong> ({result.document?.type})
                        <br />
                        <em>Preview:</em> {result.extractedText?.substring(0, 100)}...
                    </li>
                ))}
            </ul>

        </div>
    );
};

export default SearchDocument;
