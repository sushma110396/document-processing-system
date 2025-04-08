import React, { useState } from 'react';
import axios from 'axios';


const UploadForm = () => {
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState('');

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) {
      setStatus('Please select a file');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', file.name);
    formData.append('type', file.type);

    try {
      await axios.post('http://localhost:9090/documents/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setStatus('Upload successful!');
    } catch (error) {
      console.error('Upload failed:', error);
      setStatus('Upload failed');
    }
  };

  return (
    <div className="upload-container">
      <h2>Upload Document</h2>
      <form onSubmit={handleUpload} className="upload-form">
        <input type="file" onChange={handleFileChange} />
        <button type="submit">Upload</button>
      </form>
      <p className="status-text">{status}</p>
    </div>
  );
};

export default UploadForm;
