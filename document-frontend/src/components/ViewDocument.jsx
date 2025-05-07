import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';

const ViewDocument = () => {
    const { id } = useParams();
    const [text, setText] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchText = async () => {
            try {
                const response = await axios.get(`http://localhost:9090/documents/extracted-text/${id}`);
                setText(response.data);
            } catch (err) {
                setError('Failed to load extracted text.');
            } finally {
                setLoading(false);
            }
        };

        fetchText();
    }, [id]);

    return (
        <div className="view-document">
            <Link to="/" className="back-link">Back to Documents</Link>
            <h2>Extracted Text</h2>
            {loading && <p>Loading...</p>}
            {error && <p className="error">{error}</p>}
            {!loading && !error && <pre className="extracted-text-box">{text}</pre>}
        </div>
    );
};

export default ViewDocument;
