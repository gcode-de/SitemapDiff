import React from 'react';
import {Box, Button, Card, CardContent, Tooltip, Typography} from '@mui/material';
import SiteItem from './SiteItem';

import {Site} from "../types/Site.tsx";

type SiteListProps = {
    sites: Site[];
    setEditSiteId: (siteId: string) => void;
    handleCheckUrl: (crawlId: string, url: string) => void;
    handleCrawlSite: (siteId: string) => void;
}

const SiteList: React.FC<SiteListProps> = ({sites, setEditSiteId, handleCheckUrl, handleCrawlSite}) => {
    return (
        <Box sx={{display: 'flex', justifyContent: 'space-around', padding: 0, margin: 0, gap: 2, overflow: 'visible'}}>
            {sites?.map((site) => (
                <Card key={site.id}
                      sx={{display: 'flex', flexDirection: 'column', justifyContent: 'space-between', height: '75vh'}}>
                    <CardContent sx={{flex: '1 1 auto'}}>
                        <Typography variant="h5">{site.name}</Typography>
                        <Tooltip title={
                            <Box>
                                Sitemaps:
                                {site.sitemaps.map((sitemap, index) => (
                                    <Typography key={index} variant='caption' display='block'>{sitemap}</Typography>
                                ))}
                            </Box>
                        } arrow>
                            <Typography variant='caption' sx={{cursor: 'pointer'}}>
                                {site.baseURL}
                            </Typography>
                        </Tooltip>
                        <SiteItem site={site} handleCheckUrl={handleCheckUrl}/>
                    </CardContent>
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        padding: 2
                    }}>
                        <Button variant="contained" onClick={() => setEditSiteId(site.id)}>Edit Site</Button>
                        <Button variant="contained" onClick={() => handleCrawlSite(site.id)}>Crawl Now</Button>
                    </Box>
                </Card>
            ))}
        </Box>
    );
};

export default SiteList;
