import React, { useState, useEffect } from "react";
import axios from "axios";

const DownloadDocument = () => {
    const [documents, setDocuments] = useState([]);

    useEffect(() => {
        fetchDocuments();
    }, []);

    //Fetch all the documents to display on the home page
    const fetchDocuments = async () => {
        try {
            const response = await axios.get("http://localhost:9090/documents/list");
            setDocuments(response.data);
        } catch (error) {
            console.error("Failed to fetch documents:", error);
        }
    };

    //Download a particular document
    const handleDownload = async (id, name, type) => {
        try {
            const response = await axios.get(`http://localhost:9090/documents/download/${id}`, {
                responseType: "blob",
            });

            // Create a Blob object from the binary response. Set its MIME type to the given `type
            const blob = new Blob([response.data], { type });

            // Generate a temporary URL that points to the Blob in memory.
            const url = window.URL.createObjectURL(blob);

            // Create a temporary <a> (anchor) element for downloading the file.
            const a = document.createElement("a");
            a.href = url;
            a.download = name || "downloaded_file";

            // Trigger a click to start the download
            a.click();

            // Clean up the Blob URL from memory after download starts
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error("Download failed:", error);
        }
    };

    return (
        <div className="download-section">
            <h2>Download Documents</h2>
            <ul>
                {documents.map((doc) => (
                    <li key={doc.id}>
                        {doc.name} ({doc.type}){" "}
                        <button onClick={() => handleDownload(doc.id, doc.name, doc.type)}>
                            Download
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default DownloadDocument;
