import React from 'react';
import { List } from '@mui/material';
import {Site} from "../App.tsx";
import CrawlItem from "./CrawlItem.tsx";

type SiteItemProps = {
site: Site;
}
const SiteItem: React.FC<SiteItemProps> = ({site}:SiteItemProps) => {

    return (
        <List sx={{width:360}}>
            {site.crawls.map((crawl) => (
                <CrawlItem key={crawl.id} crawl={crawl} baseURL={site.baseURL}/>

            ))}
        </List>
    );
};

export default SiteItem;
