import React, {useEffect, useState} from 'react';
import SiteList from '../components/SiteList';
import {Box} from '@mui/material';
import SiteForm from "../components/SiteForm.tsx";
import Footer from "../components/Footer.tsx";
import {Site} from "../types/Site.tsx";
import {createSite, deleteSite, updateSite} from '../api';
import Typography from "@mui/material/Typography";
import LoadingSpinner from '../assets/loadingSpinner.tsx'

type HomeProps = {
    sites: Site[],
    refreshSites: () => void,
}

const Home: React.FC<HomeProps> = ({sites, refreshSites}: HomeProps) => {
    const [isAddSite, setIsAddSite] = useState<boolean>(false);
    const [editSiteId, setEditSiteId] = useState<string | null>(null);
    const [isCrawling, setIsCrawling] = useState<string[]>([]);

    useEffect(() => {
        if (isAddSite || editSiteId) {
            const scrollContainer = document.querySelector('#scrollContainer');
            if (scrollContainer) {
                scrollContainer.scrollLeft = scrollContainer.scrollWidth;
            }
        }
    }, [isAddSite, editSiteId]);

    const handleAddSite = async (formData: Site | null | undefined) => {
        try {
            await createSite(formData);
            refreshSites();
            handleAbortForm();
        } catch (error) {
            console.error('Error creating site:', error);
        }
    };

    const handleEditSite = async (formData: Site | null | undefined) => {
        try {
            await updateSite(formData);
            refreshSites();
            handleAbortForm();
        } catch (error) {
            console.error('Error updating site:', error);
        }
    };

    const handleDeleteSite = async (siteId: string | undefined) => {
        try {
            if (window.confirm('Are you sure you want to delete this site?')) {
                await deleteSite(siteId);
                refreshSites();
                handleAbortForm();
            }
        } catch (error) {
            console.error('Error deleting site:', error);
        }
    };

    const handleAbortForm = () => {
        setIsAddSite(false);
        setEditSiteId(null);
    };

    const handleCheckUrl = (crawlId: string, url: string) => {
        console.log("Toggle checkbox:", crawlId, url);
    };

    const handleCrawlSite = (siteId: string) => {
        setIsCrawling(prevState => [...prevState, siteId])
        console.log("crawl ", siteId);
    };

    const handleCrawlAllSites = () => {
        setIsCrawling(["all"])
        console.log("crawl all sites");
    };

    if (!sites || !sites.length) {
        return <Box id="scrollContainer" sx={{
            display: 'flex',
            flexDirection: 'row',
            justifyContent: 'flex-start',
            gap: 2,
            paddingTop: 2,
            height: '80vh',
            width: '95vw',
            overflowX: 'auto'
        }}>
            <Typography variant={'h4'}>Loading ...</Typography>
            <LoadingSpinner/>
        </Box>
    }

    return (
        <>
            <Box id="scrollContainer" sx={{
                display: 'flex',
                flexDirection: 'row',
                justifyContent: 'flex-start',
                gap: 2,
                paddingTop: 2,
                height: '80vh',
                width: '95vw',
                overflowX: 'auto'
            }}>
                <SiteList sites={sites} setEditSiteId={setEditSiteId} handleCheckUrl={handleCheckUrl}
                          handleCrawlSite={handleCrawlSite} isCrawling={isCrawling}/>
                {(isAddSite || editSiteId) &&
                    <Box sx={{flex: '0 0 auto'}}>
                        <SiteForm data={sites?.find(site => site.id === editSiteId)}
                                  handleAddSite={handleAddSite}
                                  handleEditSite={handleEditSite}
                                  handleDeleteSite={handleDeleteSite}
                                  handleAbortForm={handleAbortForm}
                                  refreshSites={refreshSites}/>
                    </Box>
                }
            </Box>
            <Footer setIsAddSite={setIsAddSite} handleCrawlAllSites={handleCrawlAllSites}/>
        </>
    );
};

export default Home;
