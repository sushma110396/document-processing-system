import React, { useState, useEffect } from "react";
import axios from "axios";

const DownloadDocument = () => {
  const [documents, setDocuments] = useState([]);

  useEffect(() => {
    fetchDocuments();
  }, []);


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
