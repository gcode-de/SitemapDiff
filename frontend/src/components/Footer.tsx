import React from 'react';
import { Box, Button, Toolbar } from '@mui/material';

const Footer: React.FC = () => {
    return (
        <Box sx={{ position: 'fixed', bottom: 0, width: '100%', backgroundColor: 'background.paper' }}>
            <Toolbar sx={{display: 'flex', justifyContent:'space-between'}}>
                <Button variant="contained" color="primary">Add Site</Button>
                <Button variant="contained" color="primary">Crawl All</Button>
            </Toolbar>
        </Box>
    );
};

export default Footer;
