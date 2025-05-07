import React from "react";
import axios from "axios";
import './css/UserDocuments.css';
import { useNavigate } from "react-router-dom";

const UserDocuments = ({ user, documents, onDocumentDelete }) => {
    const [viewedText, setViewedText] = React.useState(null);
    const navigate = useNavigate(); 

    // Handle file download
    const handleDownload = async (id, name, type) => {
        try {
            const response = await axios.get(`http://localhost:9090/documents/download/${id}`, {
                responseType: "blob",
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

    // Handle file delete with confirmation
    const handleDelete = async (docId) => {
        const confirmDelete = window.confirm("Are you sure you want to delete this document?");
        if (!confirmDelete) return;

        try {
            await axios.delete(`http://localhost:9090/documents/delete/${docId}`, {
                params: { userId: user.userId },
            });
            alert("Document deleted successfully");
            onDocumentDelete(); // trigger refresh in Home
        } catch (error) {
            console.error("Delete failed:", error);
            alert("You are not authorized to delete this document.");
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
                            <button id="delete" onClick={() => handleDelete(doc.id)}>Delete</button>
                            <button id="view" onClick={() => handleView(doc.id)} disabled={!doc.id}>View</button>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default UserDocuments;
