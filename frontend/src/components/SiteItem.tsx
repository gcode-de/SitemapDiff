import React from 'react';
import {List} from '@mui/material';
import {Site} from "../App.tsx";
import CrawlItem from "./CrawlItem.tsx";

type SiteItemProps = {
    site: Site;
    handleCheckUrl: (crawlId: string, url: string) => void;
}

const SiteItem: React.FC<SiteItemProps> = ({site, handleCheckUrl}: SiteItemProps) => {
    return (
        <List sx={{width: 360, maxHeight: '55vh', overflowY: 'auto'}}>
            {site.crawls.map((crawl) => (
                <CrawlItem key={crawl.id} crawl={crawl} baseURL={site.baseURL} handleCheckUrl={handleCheckUrl}/>
            ))}
        </List>
    );
};

export default SiteItem;
