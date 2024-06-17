export type Crawl = {
    id: string;
    siteId: string;
    cronId: string;
    finishedAt: string;
    content: string[],
    prevCrawlId: string | null;
    diffToPrevCrawl: { action: "add" | "remove", url: string, checked: boolean }[]
}