import React from 'react';
import { Box, Card, CardContent, Typography, Button, Tooltip } from '@mui/material';
import SiteItem from './SiteItem';
import { Site } from "../App.tsx";

type SiteListProps = {
    sites: Site[] | null | undefined;
}

const SiteList: React.FC<SiteListProps> = ({ sites }) => {
    return (
        <Box sx={{ display: 'flex', justifyContent: 'space-around', padding: 2 }}>
            {sites?.map((site) => (
                <Card key={site.id}>
                    <CardContent>
                        <Typography variant="h5">{site.name}</Typography>
                        <Tooltip title={
                            <Box>
                                Sitemaps:
                                {site.sitemaps.map((sitemap, index) => (
                                    <Typography key={index} variant='caption' display='block'>{sitemap}</Typography>
                                ))}
                            </Box>
                        } arrow>
                            <Typography variant='caption' sx={{ cursor: 'pointer' }}>
                                {site.baseURL}
                            </Typography>
                        </Tooltip>
                        <SiteItem site={site} />
                        <Box sx={{ position: 'absolute', bottom: 0, display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                            <Button variant="contained">Edit Site</Button>
                            <Button variant="contained">Crawl Now</Button>
                        </Box>
                    </CardContent>
                </Card>
            ))}
        </Box>
    );
};

export default SiteList;
