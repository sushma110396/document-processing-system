import React from "react";
import axios from "axios";
import './css/UserDocuments.css';

const UserDocuments = ({ user, documents, onDocumentDelete }) => {
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

    return (
        <div className="user-documents">
            <ul className="documents-list">
                {documents.map((doc) => (
                    <li key={doc.id}>
                        <span>{doc.name}</span>
                        <div className="doc-actions">
                            <button id="download" onClick={() => handleDownload(doc.id, doc.name, doc.type)}>Download</button>{" "}
                            <button id="delete" onClick={() => handleDelete(doc.id)}>Delete</button>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default UserDocuments;
