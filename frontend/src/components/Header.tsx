import React from 'react';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import logo from '../assets/logo.png'
import google from '../assets/google.png'
import Box from "@mui/material/Box";

import {User} from "../types/User.tsx";

type HeaderProps = {
    login: () => void;
    logout: () => void;
    user: User | null | undefined;
}

const Header: React.FC<HeaderProps> = ({login, logout, user}: HeaderProps) => {
    return (
        <AppBar position="static">
            <Toolbar sx={{
                display: 'flex',
                justifyContent: 'space-between',
                minHeight: 42,
                width: '100%',
                maxWidth: '1200px',
                marginX: 'auto'
            }}>
                <Box sx={{display: 'flex', alignItems: 'center'}}>
                    <img src={logo} className="logo" alt="SitemapDiff logo" style={{padding: 8, height: 42}}/>
                    <Typography variant="h6" component="div" sx={{flexGrow: 1}}>
                        SitemapDiff
                    </Typography>
                </Box>
                <Typography sx={{
                    display: {
                        xs: 'none',
                        s: 'none',
                        md: 'block'
                    }
                }}>Track changes in websites based on their sitemap.xml file.</Typography>
                {<Typography sx={{
                    display: {
                        xs: 'none',
                        sm: 'block'
                    }
                }}>Hello, {user ? user.name : "guest user"}!</Typography>}
                {user ? <Button color="inherit" variant="outlined" onClick={logout}>Logout</Button> :
                    <Button color="inherit" variant="outlined" onClick={login}>Login with <img src={google}
                                                                                               alt="Google logo"
                                                                                               width={16}
                                                                                               style={{marginLeft: 8}}/></Button>}

            </Toolbar>
        </AppBar>
    );
};

export default Header;
