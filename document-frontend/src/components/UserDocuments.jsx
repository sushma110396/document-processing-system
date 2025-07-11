import React from "react";
import axios from "axios";
import './css/UserDocuments.css';
import { useNavigate } from "react-router-dom";
import API_BASE_URL from './api';

const UserDocuments = ({ user, documents, onDocumentDelete }) => {
    const [viewedText, setViewedText] = React.useState(null);
    const [deleteTargetId, setDeleteTargetId] = React.useState(null);
    const [toastMessage, setToastMessage] = React.useState(null);


    const navigate = useNavigate(); 

    // Handle file download
    const handleDownload = async (id, name, type) => {
        try {
            const response = await axios.get(`${API_BASE_URL}/documents/download/${id}`, {
                responseType: "blob",
                withCredentials: true
            });

            const blob = new Blob([response.data], { type });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = name || "downloaded_file";
            a.click();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error("Download failed:", error);
        }
    };


    const handleDeleteConfirm = async () => {
        try {
              await axios.delete(`${API_BASE_URL}/documents/delete/${deleteTargetId}`, {
                params: { userId: user.userId || user.id },
                withCredentials: true

            });
            setToastMessage("Document deleted successfully"); // Show toast
            setTimeout(() => setToastMessage(null), 3000); 
            onDocumentDelete();
        } catch (error) {
            console.error("Delete failed:", error);
            alert("You are not authorized to delete this document.");
        } finally {
            setDeleteTargetId(null);
        }
    };



    // Navigate to view page
    const handleView = (docId) => {
        navigate(`/view/${docId}`);
    };

    return (
        <div className="user-documents">
            <ul className="documents-list">
                {documents.map((doc) => (
                    <li key={doc.id}>
                        <span>{doc.name}</span>
                        {doc.status === 'uploading' && <span className="doc-status uploading">Uploading...</span>}
                        {doc.status === 'uploaded' && <span className="doc-status success">{'\u2713'}</span>}
                        <div className="doc-actions">
                            <button id="download" onClick={() => handleDownload(doc.id, doc.name, doc.type)}>Download</button>{" "}
                            <button id="delete" onClick={() => setDeleteTargetId(doc.id)}>Delete</button>
                            <button id="view" onClick={() => handleView(doc.id)} disabled={!doc.id}>View</button>
                        </div>
                    </li>
                ))}
            </ul>
            {deleteTargetId && (
                <div className="modal-backdrop">
                    <div className="modal">
                        <p>Are you sure you want to delete this document?</p>
                        <div className="modal-buttons">
                            <button className="confirm" onClick={handleDeleteConfirm}>Delete</button>
                            <button className="cancel" onClick={() => setDeleteTargetId(null)}>Cancel</button>
                        </div>
                    </div>
                </div>
            )}
            {toastMessage && (
                <div className="toast-backdrop">
                    <div className="toast-success">
                        {toastMessage}
                    </div>
                </div>
            )}



        </div>
    );
};

export default UserDocuments;
