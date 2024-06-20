import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import {Container, CssBaseline, ThemeProvider} from '@mui/material';
import theme from './theme';
import Header from './components/Header';
import Home from './pages/Home';
import './App.css'
import axios from "axios";
import {defaultSites} from "./assets/defaultSites.ts";
import {User} from "./types/User.tsx";
import {Site} from "./types/Site.tsx";

const App: React.FC = () => {

    const [user, setUser] = useState<User | null | undefined>(undefined)
    const [sites, setSites] = useState<Site[]>(defaultSites)

    const loadUser = () => {
        axios.get('/api/auth/me')
            .then(response => {
                setUser(response.data)
            })
            .catch(error => {
                setUser(null)
                console.error(error);
            })
    }

    const loadSites = () => {
        axios.get(`/api/sites/${user?.id}`)
            .then(response => {
                setSites(response.data)
            })
            .catch(error => {
                // setSites([])
                console.error(error);
            })
    }

    useEffect(() => {
        loadUser();
    }, [])

    useEffect(() => {
        loadSites();
    }, [user]);


    function login() {
        const host =
            window.location.host === "localhost:5173"
                ? "http://localhost:8080"
                : window.location.origin;

        window.open(host + "/oauth2/authorization/google", "_self");
    }

    function logout() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin

        window.open(host + '/logout', '_self')
    }

    return (
        <Router>
            <ThemeProvider theme={theme}>
                <CssBaseline/>
                <Container sx={{width: '100%'}}>
                    <Header login={login} logout={logout} user={user}/>
                    <Routes>
                        <Route path="/" element={<Home sites={sites}/>}/>
                    </Routes>
                </Container>
            </ThemeProvider>
        </Router>
    );
}

export default App
