import React, { useState, useEffect } from 'react';
import UploadForm from './UploadForm';
import SearchDocument from './SearchDocument';
import axios from 'axios';
import UserDocuments from './UserDocuments';
import './css/Home.css';
import API_BASE_URL from './api';
import { useNavigate } from 'react-router-dom';


const Home = ({ onLogout, user }) => {
    //const [user, setUser] = useState(() => JSON.parse(sessionStorage.getItem("user")));
    const [documents, setDocuments] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 10;
    const [showUpload, setShowUpload] = useState(false);
    const [selectedType, setSelectedType] = useState("all");

    const navigate = useNavigate();


    const fetchDocuments = async (page = 0) => {
        try {
            //const token = sessionStorage.getItem('token');
            const response = await axios.get(`${API_BASE_URL}/documents/list`, {
                params: {
                    userId: user.userId || user.id, 
                    page: page,
                    size: pageSize,
                    type: selectedType !== "all" ? selectedType : undefined
                },
                // headers: { Authorization: `Bearer ${token}` }
                withCredentials: true
            });



            setDocuments(response.data.documents || []);
            setCurrentPage(response.data.currentPage);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Failed to fetch documents:", error);
        }
    };

    useEffect(() => {
        if (!user) {
            navigate('/login');
        }
    }, [user, navigate]);

    useEffect(() => {
        if (user) {
            fetchDocuments(0);
        }
    }, [selectedType, user]);

    if (!user) {
        return <p>Loading...</p>;
    }


    const handleTempUpload = (tempDoc) => {
        setDocuments((prev) => [tempDoc, ...prev]);
    };

    const handleUploadSuccess = (tempId) => {
        setDocuments((prev) =>
            prev.map((doc) =>
                doc.tempId === tempId ? { ...doc, status: 'uploaded' } : doc
            )
        );

 
        setTimeout(() => {
            fetchDocuments(); 
        }, 3000); 
    };

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
                <SearchDocument
                    user={user}
                    selectedType={selectedType}
                    setSelectedType={setSelectedType}
                />
                <button className="upload-button" onClick={() => setShowUpload(true)}>+ New Document</button>
            </div>


            <div className="document-wrapper">
                <div className="document-section">
                    <UserDocuments user={user} documents={documents} onDocumentDelete={() => fetchDocuments(currentPage)} />
                    <div className="pagination-controls">
                        <button disabled={currentPage === 0} onClick={() => fetchDocuments(currentPage - 1)}>Previous</button>
                        <span className="page-number">{totalPages > 0 ? (
                            <p>Page {currentPage + 1} of {totalPages}</p>
                        ) : (
                            <p>No documents to display</p>
                        )}
                        </span>
                        <button disabled={currentPage + 1 >= totalPages} onClick={() => fetchDocuments(currentPage + 1)}>Next</button>
                    </div>
                </div>
            </div>

            <UploadForm user={user} visible={showUpload} onClose={() => setShowUpload(false)} onTempUpload={handleTempUpload} onUploadSuccess={handleUploadSuccess} />
        </div>
    );
};

export default Home;
