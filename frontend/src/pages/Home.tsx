import React, {useState} from 'react';
import SiteList from '../components/SiteList';
import {Box} from '@mui/material';
import SiteForm from "../components/SiteForm.tsx";
import Footer from "../components/Footer.tsx";
import {Site} from "../types/Site.tsx";

type HomeProps = {
    sites: Site[];
    refreshSites: () => void;
}

const Home: React.FC<HomeProps> = ({sites, refreshSites}: HomeProps) => {
    const [isAddSite, setIsAddSite] = useState<boolean>(false);
    const [editSiteId, setEditSiteId] = useState<string | null>(null);

    const handleAddSite = (formData: Site | null | undefined): Site => {
        if (formData) {
            setIsAddSite(false);
            console.log(formData);
            return formData;
        }
        throw new Error("Form data is null or undefined");
    }

    const handleEditSite = (formData: Site | null | undefined): Site => {
        if (formData) {
            setEditSiteId(null);
            console.log(formData);
            return formData;
        }
        throw new Error("Form data is null or undefined");
    }

    const handleDeleteSite = (siteId: string | undefined): string => {
        if (siteId) {
            setIsAddSite(false);
            setEditSiteId(null);
            console.log("Delete Site:", siteId)
            return siteId;
        }
        throw new Error("Site ID is null or undefined");
    }

    const handleAbortForm = () => {
        setIsAddSite(false);
        setEditSiteId(null);
    }

    const handleCheckUrl = (crawlId: string, url: string) => {
        console.log("Toggle checkbox:", crawlId, url);
        return;
    }

    const handleCrawlSite = (siteId: string) => {
        console.log("crawl ", siteId);
    }

    const handleCrawlAllSites = () => {
        console.log("crawl all sites");
    }

    return (
        <>
            <Box sx={{
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
                          handleCrawlSite={handleCrawlSite}/>
                {(isAddSite || editSiteId) &&
                    <Box sx={{flex: '0 0 auto'}}>
                        <SiteForm data={sites?.find(site => site.id === editSiteId)} handleAddSite={handleAddSite}
                                  handleEditSite={handleEditSite} handleDeleteSite={handleDeleteSite}
                                  handleAbortForm={handleAbortForm} refreshSites={refreshSites}/>
                    </Box>
                }
            </Box>
            <Footer setIsAddSite={setIsAddSite} handleCrawlAllSites={handleCrawlAllSites}/>
        </>
    );
};

export default Home;
