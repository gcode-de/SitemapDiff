import {Crawl} from "./Crawl.tsx";

export type Site = {
    id?: string;
    name: string;
    baseURL: string;
    sitemaps: string[];
    userId: string;
    scrapeCron: string;
    crawls: Crawl[]
}