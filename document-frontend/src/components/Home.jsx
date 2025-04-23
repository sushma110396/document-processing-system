import React, { useState, useEffect } from 'react';
import UploadForm from './UploadForm';
import DocumentList from './DocumentList';
import SearchDocument from './SearchDocument';
import axios from 'axios';


const Home = ({ user, onLogout }) => {
    const [documents, setDocuments] = useState([]);

    // Fetch user documents
    const fetchDocuments = async () => {
        try {
            const response = await axios.get('http://localhost:9090/documents/list', {
                params: { userId: user.userId },
            });
            if (Array.isArray(response.data)) {
                setDocuments(response.data);
            } else {
                setDocuments([]);
            }
        } catch (error) {
            console.error("Failed to fetch documents:", error);
        }
    };

    useEffect(() => {
        fetchDocuments();
    }, []);

    return (
        <div>
            <h1>Document Processing System</h1>
            <p>Welcome, {user.username}!</p>
            <button onClick={onLogout}>Logout</button>
            <UploadForm user={user} onUploadSuccess={fetchDocuments} />
            <DocumentList user={user} documents={documents} onDocumentDelete={fetchDocuments} />
            <SearchDocument user={user} />
        </div>
    );
};

export default Home;
