import React, {useEffect, useState} from 'react';
import SiteList from '../components/SiteList';
import {Box} from '@mui/material';
import Footer from "../components/Footer.tsx";
import {Site} from "../types/Site.tsx";
import {createSite, deleteSite, updateSite} from '../api';
import Typography from "@mui/material/Typography";
import LoadingSpinner from '../assets/loadingSpinner'
import axios from 'axios';

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
            const createdSite = await createSite(formData);
            handleAbortForm();
            refreshSites();
            await handleCrawlSite(createdSite.id)
        } catch (error) {
            console.error('Error creating site:', error);
        }
    };

    const handleEditSite = async (formData: Site | null | undefined) => {
        try {
            await updateSite(formData);
            handleAbortForm();
            refreshSites();
        } catch (error) {
            console.error('Error updating site:', error);
        }
    };

    const handleDeleteSite = async (siteId: string | undefined) => {
        try {
            if (window.confirm('Are you sure you want to delete this site?')) {
                await deleteSite(siteId);
                handleAbortForm();
                refreshSites();
            }
        } catch (error) {
            console.error('Error deleting site:', error);
        }
    };

    const handleAbortForm = () => {
        setIsAddSite(false);
        setEditSiteId(null);
    };

    const handleCheckUrl = async (crawlId: string, url: string, newState: boolean) => {
        console.log("Toggle checkbox:", crawlId, url, newState);

        const payload = {
            url: url,
            checked: newState
        };

        try {
            const response = await axios.put(`/api/crawls/update-url-status/${crawlId}`, payload);
            console.log("URL checked status updated successfully:", response.data);
        } catch (error) {
            console.error("Error updating URL checked status:", error);
        }
        refreshSites();
    };

    const handleCrawlSite = async (siteId: string) => {
        setIsCrawling(prevState => [...prevState, siteId]);
        console.log("crawl ", siteId);

        try {
            const response = await axios.get(`/api/crawls/start/${siteId}`);
            console.log("Crawl site response:", response.data);
        } catch (error) {
            console.error("Error crawling site:", error);
        } finally {
            setIsCrawling(prevState => prevState.filter(e => e !== siteId));
            refreshSites();
        }
    };

    const handleCrawlAllSites = async () => {
        sites.forEach((site: Site) => {
            handleCrawlSite(site.id)
        })
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
                padding: 2,
                height: 'calc(100% - 100px)',
                width: '100vw',
                overflowX: 'auto'
            }}>
                <SiteList
                    sites={sites}
                    setEditSiteId={setEditSiteId}
                    handleCheckUrl={handleCheckUrl}
                    handleCrawlSite={handleCrawlSite}
                    isCrawling={isCrawling}
                    isAddSite={isAddSite}
                    editSiteId={editSiteId}
                    handleAbortForm={handleAbortForm}
                    refreshSites={refreshSites}
                    handleAddSite={handleAddSite}
                    handleEditSite={handleEditSite}
                    handleDeleteSite={handleDeleteSite}
                />
            </Box>
            <Footer setIsAddSite={setIsAddSite} handleCrawlAllSites={handleCrawlAllSites} isCrawling={isCrawling}/>
        </>
    );
};

export default Home;
