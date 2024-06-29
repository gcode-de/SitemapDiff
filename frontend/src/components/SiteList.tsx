import React from 'react';
import {Box, Button, Card, CardContent, Divider, Tooltip, Typography} from '@mui/material';
import SiteItem from './SiteItem';
import SiteForm from './SiteForm'; // Import the SiteForm component

import {Site} from "../types/Site.tsx";
import LoadingSpinner from "../assets/loadingSpinner.tsx";

type SiteListProps = {
    sites: Site[];
    setEditSiteId: (siteId: string) => void;
    handleCheckUrl: (crawlId: string, url: string, newState: boolean) => void;
    handleCrawlSite: (siteId: string) => void;
    isCrawling: string[];
    isAddSite: boolean; // Add a new prop to determine if the SiteForm should be displayed
    editSiteId: string | null; // Add a prop to hold the id of the site being edited
    handleAbortForm: () => void;
    refreshSites: () => void;
    handleAddSite: (site: Site | undefined | null) => void;
    handleEditSite: (site: Site | undefined | null) => void;
    handleDeleteSite: (id: string) => void;
}

const SiteList: React.FC<SiteListProps> = ({
                                               sites,
                                               setEditSiteId,
                                               handleCheckUrl,
                                               handleCrawlSite,
                                               isCrawling,
                                               isAddSite,
                                               editSiteId,
                                               handleAbortForm,
                                               refreshSites,
                                               handleAddSite,
                                               handleEditSite,
                                               handleDeleteSite
                                           }) => {
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
            {(isAddSite || editSiteId) && (
                <Box sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    height: 'calc(100vh - 160px)',
                    scrollSnapAlign: 'start',
                    minWidth: '300px',
                    flex: '0 0 auto'
                }}>
                    <SiteForm
                        handleAbortForm={handleAbortForm}
                        data={sites.find(site => site.id === editSiteId) || null}
                        refreshSites={refreshSites}
                        handleAddSite={handleAddSite}
                        handleEditSite={handleEditSite}
                        handleDeleteSite={handleDeleteSite}
                    />
                </Box>
            )}
        </Box>
    );
};

export default SiteList;
