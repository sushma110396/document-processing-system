import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './css/UploadForm.css';

const UploadForm = ({ user, onUploadSuccess, visible, onClose }) => {
    const [file, setFile] = useState(null);
    const [status, setStatus] = useState('');
    const [uploading, setUploading] = useState(false);

    useEffect(() => {
        if (visible) {
            setStatus('');
            setFile(null);
        }
    }, [visible]);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setStatus('');
    };

    const handleUpload = async (e) => {
        e.preventDefault();

        if (!file) {
            setStatus('Please select a file');
            return;
        }

        if (file.size > 200 * 1024 * 1024) {
            setStatus('File size exceeds 200MB. Please upload a smaller file.');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('name', file.name);
        formData.append('type', file.type);
        formData.append('userId', user.userId);

        try {
            setUploading(true);
            await axios.post('http://localhost:9090/documents/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            setStatus('Upload successful!');
            setFile(null);
            onUploadSuccess(); // refresh doc list
        } catch (error) {
            console.error('Upload failed:', error);
            setStatus('Upload failed');
        } finally {
            setUploading(false);
        }
    };

    if (!visible) return null;

    return (
        <div className="upload-modal">
            <div className="upload-box">
                <h3 className="modal-title">Upload Document</h3>
                <form onSubmit={handleUpload} className="upload-form">
                    <input type="file" onChange={handleFileChange} className="upload-file" />
                    <div className="upload-buttons">
                        <button type="submit" disabled={uploading} id="submit">
                            {uploading ? 'Uploading...' : 'Upload'}
                        </button>
                        <button type="button" className="cancel-btn" onClick={onClose}>
                            {status === 'Upload successful!' ? 'Close' : 'Cancel'}
                        </button>
                    </div>
                </form>
                {status && <p className="status-text">{status}</p>}
            </div>
        </div>
    );
};

export default UploadForm;
