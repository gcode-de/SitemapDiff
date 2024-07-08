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
    isAddSite: boolean;
    editSiteId: string | null;
    handleAbortForm: () => void;
    refreshSites: () => void;
    handleAddSite: (site: Site | undefined | null) => void;
    handleEditSite: (site: Site | undefined | null) => void;
    handleDeleteSite: (id: string) => void;
    userMail: string | undefined;
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
                                               handleDeleteSite,
                                               userMail,
                                           }) => {
    return (
        <Box id="scrollContainer" sx={{
            display: 'flex',
            justifyContent: 'start',
            padding: 0,
            margin: 0,
            gap: 2,
            overflowX: 'auto',
            scrollSnapType: 'x mandatory',
            scrollPadding: '16px',
            height: 'calc(100vh - 160' +
                'px)',
        }}>
            {sites?.map((site) => (
                <Card key={site.id}
                      sx={{
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'space-between',
                          height: 'calc(100vh - 165' +
                              'px)',
                          scrollSnapAlign: 'start',
                          minWidth: '300px',
                          flex: '0 0 auto'
                      }}>
                    <CardContent sx={{flex: '1 1 auto'}}>
                        <Box sx={{display: 'flex', alignItems: 'center', gap: 1}}>
                            {site.favicon && <Box
                                component="img"
                                src={site.favicon}
                                alt="favicon"
                                width="16"
                                height="16"
                                sx={{
                                    display: "inline",
                                    marginRight: 1,
                                    width: 24,
                                    height: 24,
                                }}
                            />}
                            <Typography variant="h5">{site.name}</Typography>
                        </Box>
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
                        <SiteItem site={site} handleCheckUrl={handleCheckUrl} refreshSites={refreshSites}/>
                    </CardContent>
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        padding: 2
                    }}>
                        <Button variant="outlined" disabled={editSiteId !== null || isAddSite}
                                onClick={() => setEditSiteId(site.id)}>Edit Site</Button>
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
                        userMail={userMail}
                    />
                </Box>
            )}
        </Box>
    );
};

export default SiteList;
