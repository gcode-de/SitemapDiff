import {AppBar, Box, Button, Toolbar} from '@mui/material';
import React from "react";

type FooterProps = {
    setIsAddSite: (state: boolean) => void;
    handleCrawlAllSites: () => void;
    editSiteId: string | null;
    readOnly?: boolean;
}

const Footer: React.FC<FooterProps> = ({setIsAddSite, handleCrawlAllSites, editSiteId, readOnly = false}: FooterProps) => {
    return (
        <AppBar position="fixed" sx={{
            top: 'auto', bottom: 0, bgcolor: 'background.default',
            color: 'text.primary'
        }}>
            <Toolbar sx={{
                display: 'flex',
                justifyContent: 'space-between',
                minHeight: 42,
                width: '100%',
                maxWidth: '1200px',
                margin: '0 auto',
                bgcolor: 'background.default',
                color: 'text.primary'
            }}>
                <Box sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    width: '100%',
                }}>
                    <Button variant="contained" disabled={readOnly || editSiteId !== null} color="primary"
                            onClick={() => setIsAddSite(true)}>Add Site</Button>
                    <Button variant="contained" color="primary" disabled={readOnly}
                            onClick={handleCrawlAllSites}>Crawl All</Button>
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default Footer;
