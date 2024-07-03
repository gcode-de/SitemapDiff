// import React, {useEffect, useRef} from 'react';
import {List} from '@mui/material';
import CrawlItem from "./CrawlItem";
import {Site} from "../types/Site";

type SiteItemProps = {
    site: Site;
    handleCheckUrl: (crawlId: string, url: string, newState: boolean) => void;
    refreshSites: () => void;
}

const SiteItem: React.FC<SiteItemProps> = ({site, handleCheckUrl, refreshSites}: SiteItemProps) => {
    // const listRef = useRef<HTMLUListElement>(null);
    //
    // useEffect(() => {
    //     if (listRef.current) {
    //         listRef.current.scrollTop = listRef.current.scrollHeight;
    //     }
    // }, [site.crawls.length]);

    return (
        <List
            // ref={listRef}
            sx={{
                width: 360,
                maxHeight: 'calc(100vh - 320px)',
                overflowY: 'auto'
            }}>
            {site.crawls?.map((crawl) => (
                <CrawlItem key={crawl.id + crawl.finishedAt} crawl={crawl} baseURL={site.baseURL}
                           handleCheckUrl={handleCheckUrl} refreshSites={refreshSites}/>
            ))}
        </List>
    );
};

export default SiteItem;
