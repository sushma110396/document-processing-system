import React from 'react';
import UploadForm from '../components/UploadForm';

const Home = () => {
  return (
    <div>
      <h1>Document Processing System</h1>
      <UploadForm />
       <DownloadDocument/>
    </div>
  );
};

export default Home;
