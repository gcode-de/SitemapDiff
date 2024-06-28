import React from 'react';
import {List} from '@mui/material';
import CrawlItem from "./CrawlItem.tsx";
import {Site} from "../types/Site.tsx";

type SiteItemProps = {
    site: Site;
    handleCheckUrl: (crawlId: string, url: string, newState: boolean) => void;
}

const SiteItem: React.FC<SiteItemProps> = ({site, handleCheckUrl}: SiteItemProps) => {
    return (
        <List sx={{width: 360, maxHeight: '55vh', overflowY: 'auto'}}>
            {site.crawls?.map((crawl) => (
                <CrawlItem key={crawl.id + crawl.finishedAt} crawl={crawl} baseURL={site.baseURL}
                           handleCheckUrl={handleCheckUrl}/>
            ))}
        </List>
    );
};

export default SiteItem;
