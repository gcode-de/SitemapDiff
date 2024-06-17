import React, {useState} from 'react';
import SiteList from '../components/SiteList';
import {Box} from '@mui/material';
import SiteForm from "../components/SiteForm.tsx";
import {Site} from "../App.tsx";
import Footer from "../components/Footer.tsx";

type HomeProps = {
    sites: Site[] | null | undefined;
}

const Home: React.FC<HomeProps> = ({sites}: HomeProps) => {
    const [isAddSite, setIsAddSite] = useState<boolean>(false);
    const [editSiteId, setEditSiteId] = useState<string | null>(null);

    const handleAddSite = (formData: Site): Site => {
        setIsAddSite(false);
        console.log(formData)
        return formData;
    }

    const handleEditSite = (formData: Site): Site => {
        setEditSiteId(null);
        console.log(formData)
        return formData;
    }

    const handleDeleteSite = (siteId: string): string => {
        return siteId;
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
                overflowX: 'auto' // Use overflowX for horizontal scrolling
            }}>
                <SiteList sites={sites} setEditSiteId={setEditSiteId} handleCheckUrl={handleCheckUrl}
                          handleCrawlSite={handleCrawlSite}/>
                {(isAddSite || editSiteId) &&
                    <Box sx={{flex: '0 0 auto'}}>
                        <SiteForm data={sites?.find(site => site.id === editSiteId)} handleAddSite={handleAddSite}
                                  handleEditSite={handleEditSite} handleDeleteSite={handleDeleteSite}
                                  handleAbortForm={handleAbortForm}/>
                    </Box>
                }
            </Box>
            <Footer setIsAddSite={setIsAddSite}/>
        </>
    );
};

export default Home;
