import React from 'react';
import SiteList from '../components/SiteList';
import { Box } from '@mui/material';
import SiteForm from "../components/SiteForm.tsx";
import {Site} from "../App.tsx";

type HomeProps = {
    sites: Site[] | null | undefined;
}

const Home: React.FC<HomeProps> = ({sites}:HomeProps) => {
    return (
        <Box sx={{ display: 'flex', flexDirection: 'row', justifyContent: 'flex-start', padding: 2 }}>
            <SiteList sites={sites}/>
            <SiteForm />
        </Box>
    );
};

export default Home;