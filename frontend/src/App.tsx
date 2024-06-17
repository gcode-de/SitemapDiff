import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import {Container, CssBaseline} from '@mui/material';
import Header from './components/Header';
import Home from './pages/Home';
import './App.css'
import axios from "axios";

export type User = {
    name: string,
    email: string,
    id: string,
    picture: string
}

export type Site = {
    id: string;
    name: string;
    baseURL: string;
    sitemaps: string[];
    userId: string;
    scrapeCron: string;
    crawls: Crawl[]
}

export type Crawl = {
    id: string;
    siteId: string;
    cronId: string;
    finishedAt: string;
    content: string[],
    prevCrawlId: string | null;
    diffToPrevCrawl: { action: "add" | "remove", url: string, checked: boolean }[]
}

const defaultSites: Site[] = [
    {
        id: "1",
        name: "Google",
        baseURL: "https://google.com",
        sitemaps: ["https://google.com/sitemap.xml", "https://google.com/sitemap.xml", "https://google.com/sitemap.xml", "https://google.com/sitemap.xml"],
        userId: "user",
        scrapeCron: "string",
        crawls: [
            {
                id: "1",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-15 12:00",
                content: [],
                prevCrawlId: null,
                diffToPrevCrawl: [],
            },
            {
                id: "2",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-16 12:00",
                content: [],
                prevCrawlId: "1",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/bratwurst",
                        checked: true
                    }
                ],
            },
            {
                id: "3",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-17 12:00",
                content: [],
                prevCrawlId: "2",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/döner",
                        checked: false
                    },
                    {
                        action: "remove",
                        url: "/currywurst",
                        checked: false
                    }
                ],
            },
            {
                id: "4",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-17 14:00",
                content: [],
                prevCrawlId: "3",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/hier-kommt-eine-richtig-lange-url-die-aus-seo-gründen-so-lang-ist",
                        checked: false
                    },
                    {
                        action: "remove",
                        url: "/gulasch",
                        checked: false
                    }
                ],
            },
        ]
    },
    {
        id: "2",
        name: "Yahoo",
        baseURL: "https://yahoo.com",
        sitemaps: ["https://google.com/sitemap.xml", "https://google.com/sitemap.xml", "https://google.com/sitemap.xml", "https://google.com/sitemap.xml"],
        userId: "user",
        scrapeCron: "string",
        crawls: [
            {
                id: "1",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-15 12:00",
                content: [],
                prevCrawlId: null,
                diffToPrevCrawl: [],
            },
            {
                id: "2",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-16 12:00",
                content: [],
                prevCrawlId: "1",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/bratwurst",
                        checked: true
                    }
                ],
            },
            {
                id: "3",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-17 12:00",
                content: [],
                prevCrawlId: "2",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/döner",
                        checked: false
                    },
                    {
                        action: "remove",
                        url: "/currywurst",
                        checked: false
                    }
                ],
            },
            {
                id: "4",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-17 14:00",
                content: [],
                prevCrawlId: "3",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/hier-kommt-eine-richtig-lange-url-die-aus-seo-gründen-so-lang-ist",
                        checked: false
                    },
                    {
                        action: "remove",
                        url: "/gulasch",
                        checked: false
                    }
                ],
            },
            {
                id: "5",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-16 12:00",
                content: [],
                prevCrawlId: "4",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/bratwurst",
                        checked: true
                    }
                ],
            },
            {
                id: "6",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-17 12:00",
                content: [],
                prevCrawlId: "5",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/döner",
                        checked: false
                    },
                    {
                        action: "remove",
                        url: "/currywurst",
                        checked: false
                    }
                ],
            },
            {
                id: "7",
                siteId: "1",
                cronId: "1",
                finishedAt: "2024-06-17 14:00",
                content: [],
                prevCrawlId: "6",
                diffToPrevCrawl: [
                    {
                        action: "add",
                        url: "/hier-kommt-eine-richtig-lange-url-die-aus-seo-gründen-so-lang-ist",
                        checked: false
                    },
                    {
                        action: "remove",
                        url: "/gulasch",
                        checked: false
                    }
                ],
            },
        ]
    },
]

const App: React.FC = () => {

    const [user, setUser] = useState<User | null | undefined>(undefined)
    const [sites, setSites] = useState<Site[] | null | undefined>(defaultSites)

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
            <CssBaseline/>
            <Container sx={{width: '100%'}}>
                <Header login={login} logout={logout} user={user}/>
                <Routes>
                    <Route path="/" element={<Home sites={sites}/>}/>
                </Routes>
            </Container>
        </Router>
    );
}

export default App
