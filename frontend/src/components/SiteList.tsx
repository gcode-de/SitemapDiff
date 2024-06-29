import React from 'react';
import {Box, Button, Card, CardContent, Divider, Tooltip, Typography} from '@mui/material';
import SiteItem from './SiteItem';

import {Site} from "../types/Site.tsx";
import LoadingSpinner from "../assets/loadingSpinner.tsx";

type SiteListProps = {
    sites: Site[];
    setEditSiteId: (siteId: string) => void;
    handleCheckUrl: (crawlId: string, url: string, newState: boolean) => void;
    handleCrawlSite: (siteId: string) => void;
    isCrawling: string[];
}

const SiteList: React.FC<SiteListProps> = ({sites, setEditSiteId, handleCheckUrl, handleCrawlSite, isCrawling}) => {
    return (
        <Box sx={{
            display: 'flex',
            justifyContent: 'start',
            padding: 0,
            margin: 0,
            gap: 2,
            overflowX: 'auto',
            scrollSnapType: 'x mandatory',
            scrollPadding: '16px'
        }}>
            {sites?.map((site) => (
                <Card key={site.id}
                      sx={{
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'space-between',
                          height: 'calc(100vh - 160px)',
                          scrollSnapAlign: 'start',
                          minWidth: '300px',
                          flex: '0 0 auto'
                      }}>
                    <CardContent sx={{flex: '1 1 auto'}}>
                        <Typography variant="h5">{site.name}</Typography>
                        <Tooltip title={
                            <Box>
                                <Typography variant='caption' display='block'>Sitemap: {site.sitemap}</Typography>
                            </Box>
                        } arrow>
                            <Typography variant='caption' sx={{cursor: 'pointer'}}>
                                {site.baseURL}
                            </Typography>
                        </Tooltip>
                        <Divider/>
                        <SiteItem site={site} handleCheckUrl={handleCheckUrl}/>
                    </CardContent>
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        padding: 2
                    }}>
                        <Button variant="outlined" onClick={() => setEditSiteId(site.id)}>Edit Site</Button>
                        {isCrawling.some(e => e === "all" || e === site.id) && <LoadingSpinner/>}
                        <Button variant="outlined" disabled={isCrawling.some(e => e === "all" || e === site.id)}
                                onClick={() => handleCrawlSite(site.id)}>Crawl Now</Button>
                    </Box>
                </Card>
            ))}
        </Box>
    );
};

export default SiteList;
