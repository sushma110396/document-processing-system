import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './css/SearchDocument.css';

const SearchDocument = ({ user, selectedType, setSelectedType }) => {
    const [query, setQuery] = useState('');
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const navigate = useNavigate();

    const types = ["all", "pdf", "docx", "image"];

    const handleSearch = (e) => {
        e.preventDefault();
        const encodedQuery = encodeURIComponent(query);
        const encodedType = encodeURIComponent(selectedType);
        const url = `/search-results?q=${encodedQuery}&userId=${user.userId}&type=${encodedType}`;
        navigate(url);
    };

    const toggleDropdown = () => {
        setDropdownOpen(!dropdownOpen);
    };

    const handleTypeSelect = (type) => {
        setSelectedType(type);
        setDropdownOpen(false);
    };

    return (
        <form onSubmit={handleSearch} className="search-bar-container">
            <div className="search-bar">
                <div className="custom-dropdown" onClick={toggleDropdown}>
                    <div className="dropdown-selected">{selectedType.toUpperCase()} <span className={`dropdown-icon ${dropdownOpen ? 'open' : ''}`}>&#9660;</span></div>
                    {dropdownOpen && (
                        <ul className="dropdown-options">
                            {types.map((type) => (
                                <li key={type} className={`dropdown-option${type === selectedType ? ' selected' : ''}`} onClick={(e) => {
                                        e.stopPropagation();
                                        handleTypeSelect(type);
                                    }}>
                                    {type.toUpperCase()}
                                </li>

                            ))}
                        </ul>
                    )}
                </div>

                <input type="text" placeholder="Search documents..." value={query} onChange={(e) => setQuery(e.target.value)} className="search-input-combined"/>

                <button type="submit" className="search-button-combined"> Search </button>
            </div>
        </form>
    );
};

export default SearchDocument;
