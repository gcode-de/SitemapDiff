import { Button, Toolbar } from '@mui/material';
import React from "react";

type FooterProps = {
    setIsAddSite: any;
}
const Footer: React.FC<FooterProps> = ({setIsAddSite}:FooterProps) => {
    return (
            <Toolbar sx={{position: 'fixed', bottom: 0, width: '100%',display: 'flex', justifyContent:'space-between', margin:0, padding:0}}>
                <Button variant="contained" color="primary" onClick={() => setIsAddSite(true)}>Add Site</Button>
                <Button variant="contained" color="primary">Crawl All</Button>
            </Toolbar>
    );
};

export default Footer;
