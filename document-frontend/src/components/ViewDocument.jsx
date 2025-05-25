import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
import './css/ViewDocument.css'; 

const ViewDocument = () => {
    const { id } = useParams();
    const [text, setText] = useState('');
    const [metadata, setMetadata] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const getReadableType = (mimeType) => {
        const map = {
            'application/pdf': 'PDF',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'DOCX',
            'application/msword': 'DOC',
            'application/vnd.ms-excel': 'XLS',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'XLSX',
            'image/png': 'PNG',
            'image/jpeg': 'JPG'
        };

        return map[mimeType] || mimeType;
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [textRes, metaRes] = await Promise.all([
                    axios.get(`https://document-processing-system.onrender.com/extracted-text/${id}`),
                    axios.get(`https://document-processing-system.onrender.com/documents/metadata/${id}`)
                ]);
                setText(textRes.data);
                setMetadata(metaRes.data);
            } catch (err) {
                setError('Failed to load document.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [id]);

    return (
        <div className="view-document-page">
            <Link to="/" className="back-button">Back to Documents</Link>
            <h1 className="page-title">Document Details</h1>

            {loading && <p>Loading...</p>}
            {error && <p className="error">{error}</p>}

            {!loading && !error && metadata && (
                <div className="metadata-card">
                    <h2>Metadata</h2>
                    <div className="metadata-grid">
                        <div><span className="label">Name:</span> {metadata.name}</div>
                        <div><span className="label">Type:</span> {getReadableType(metadata.type)}</div>
                        <div><span className="label">Uploaded By:</span> {metadata.uploadedBy}</div>
                        <div><span className="label">Uploaded On:</span> { new Date(metadata.uploadedOn).toLocaleString() }</div>
                        <div><span className="label">Status:</span> {metadata.status}</div>
                    </div>
                </div>
            )}

            {!loading && !error && (
                <div className="extracted-text-section">
                    <h2>Extracted Text</h2>
                    <div className="extracted-text-box">
                        <pre>{text}</pre>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ViewDocument;
