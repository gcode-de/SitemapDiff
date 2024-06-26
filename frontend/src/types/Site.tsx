import {Crawl} from "./Crawl.tsx";

export type Site = {
    id: string;
    name: string;
    baseURL: string;
    sitemap: string;
    userId: string;
    scrapeCron: string;
    crawls: Crawl[]
}