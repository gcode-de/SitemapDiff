import React, {useEffect, useRef} from 'react';
import {List} from '@mui/material';
import CrawlItem from "./CrawlItem";
import {Site} from "../types/Site";

type SiteItemProps = {
    site: Site;
    handleCheckUrl: (crawlId: string, url: string, newState: boolean) => void;
}

const SiteItem: React.FC<SiteItemProps> = ({site, handleCheckUrl}: SiteItemProps) => {
    const listRef = useRef<HTMLUListElement>(null);

    useEffect(() => {
        if (listRef.current) {
            listRef.current.scrollTop = listRef.current.scrollHeight;
        }
    }, [site.crawls.length]);

    return (
        <List ref={listRef} sx={{width: 360, maxHeight: '55vh', overflowY: 'auto'}}>
            {site.crawls?.map((crawl) => (
                <CrawlItem key={crawl.id + crawl.finishedAt} crawl={crawl} baseURL={site.baseURL}
                           handleCheckUrl={handleCheckUrl}/>
            ))}
        </List>
    );
};

export default SiteItem;
