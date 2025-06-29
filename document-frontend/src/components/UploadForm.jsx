import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './css/UploadForm.css';
import API_BASE_URL from './api';

const UploadForm = ({ user, onUploadSuccess, visible, onClose, onTempUpload }) => {
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

        const tempId = Date.now();
        onTempUpload({ tempId, name: file.name, status: 'uploading' });
        onClose(); 

        const formData = new FormData();
        formData.append('file', file);
        formData.append('name', file.name);
        formData.append('type', file.type);
        formData.append('userId', user.userId);

       
        try {
            setUploading(true);
            const res = await axios.post(`${API_BASE_URL}/documents/upload`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                withCredentials: true,
            });

            console.log("Upload success - backend response:", res.data);
            onUploadSuccess(tempId, res.data);  // pass entire document
        } catch (error) {
            console.error('Upload failed:', error);
            alert('Upload failed');
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
                            Cancel
                        </button>
                    </div>
                </form>
                {status && <p className="status-text">{status}</p>}
            </div>
        </div>
    );
};

export default UploadForm;
