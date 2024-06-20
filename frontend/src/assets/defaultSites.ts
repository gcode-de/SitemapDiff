import {Site} from "../types/Site.tsx";

export const defaultSites: Site[] = [
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