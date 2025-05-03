import React, { useState, useEffect } from 'react';
import UploadForm from './UploadForm';
import SearchDocument from './SearchDocument';
import axios from 'axios';
import UserDocuments from './UserDocuments';
import './css/Home.css';

const Home = ({ user, onLogout }) => {
    const [documents, setDocuments] = useState([]);
    const [showUpload, setShowUpload] = useState(false);

    const fetchDocuments = async () => {
        try {
            const response = await axios.get('http://localhost:9090/documents/list', {
                params: { userId: user.userId },
            });
            setDocuments(Array.isArray(response.data) ? response.data : []);
        } catch (error) {
            console.error("Failed to fetch documents:", error);
        }
    };

    useEffect(() => {
        fetchDocuments();
    }, []);

    return (
        <div className="home-page">
            <div className="header-wrapper">
                <div className="logout-container">
                    <button className="logout-button" onClick={onLogout}>Logout</button>
                </div>
                <p className="welcome-message">Welcome, {user.username}!</p>
            </div>

            <h2 className="page-title">Documents</h2>
            <div className="search-upload-bar">
                <SearchDocument user={user} />
                <button className="upload-button" onClick={() => setShowUpload(true)}>+ New Document</button>
            </div>

            <div className="document-wrapper">
                <div className="document-section">
                <UserDocuments user={user} documents={documents} onDocumentDelete={fetchDocuments} />
                </div>
            </div>

            <UploadForm user={user} onUploadSuccess={fetchDocuments} visible={showUpload} onClose={() => setShowUpload(false)} />

        </div>
    );
};

export default Home;
