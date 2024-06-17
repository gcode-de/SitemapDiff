import {Box, Button, Toolbar} from '@mui/material';
import React from "react";

type FooterProps = {
    setIsAddSite: (state: boolean) => void;
    handleCrawlAllSites: () => void;
}
const Footer: React.FC<FooterProps> = ({setIsAddSite, handleCrawlAllSites}: FooterProps) => {
    return (
        <Toolbar sx={{
            position: 'fixed',
            bottom: 0,
            width: '100%',
        }}>
            <Box sx={{
                maxWidth: '100vw',
                width: '90%',
                display: 'flex',
                justifyContent: 'space-between',
                margin: 0,
                padding: 0
            }}>
                <Button variant="contained" color="primary" onClick={() => setIsAddSite(true)}>Add Site</Button>
                <Button variant="contained" color="primary" onClick={handleCrawlAllSites}>Crawl All</Button>
            </Box>
        </Toolbar>
    );
};

export default Footer;
