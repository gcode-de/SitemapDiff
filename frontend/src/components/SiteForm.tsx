import React, {ChangeEvent, useEffect, useState} from 'react';
import {Box, Button, Card, CardContent, TextField, Typography} from '@mui/material';
import {Site} from '../types/Site';
import {fetchSitemap} from '../api';

type SiteFormProps = {
    handleAbortForm: () => void,
    data: Site | null | undefined,
    refreshSites: () => void,
    handleAddSite: (site: Site | undefined | null) => void,
    handleEditSite: (site: Site | undefined | null) => void,
    handleDeleteSite: (id: string) => void,
}

const SiteForm: React.FC<SiteFormProps> = ({
                                               handleAbortForm,
                                               data,
                                               refreshSites,
                                               handleAddSite,
                                               handleEditSite,
                                               handleDeleteSite
                                           }: SiteFormProps) => {

    const [formData, setFormData] = useState<Site | undefined | null>(data);
    const [isFormValid, setIsFormValid] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormData((prevFormData) => ({
            ...prevFormData,
            [name]: value,
        } as Site));
    };

    useEffect(() => {
        if (formData?.name && formData?.baseURL && formData?.sitemap) {
            setIsFormValid(true);
        } else {
            setIsFormValid(false);
        }
    }, [formData]);

    const isURLValid = (url: string | undefined): boolean => {
        if (!url) return false;
        const pattern = /^(https?:\/\/)/;
        return pattern.test(url);
    };

    const findSitemapByBaseURL = async (url: string | undefined): Promise<string> => {
        if (!url) throw new Error("No URL provided.");
        if (!isURLValid(url)) {
            setError("URL must start with http:// or https://");
            return "";
        }

        try {
            const sitemap = await fetchSitemap(url);
            setFormData({...formData, sitemap} as Site);
            setError(null);
            return sitemap;
        } catch (error: any) {
            setError(error.response ? error.response.data : "Error finding sitemap");
            throw new Error("Could not retrieve sitemap.");
        }
    };

    const handleSubmit = async () => {
        if (data) {
            await handleEditSite(formData);
        } else {
            await handleAddSite(formData);
        }
        refreshSites();
        handleAbortForm();
    };

    return (
        <Card key={'siteForm'} sx={{width: '360px'}}>
            <CardContent>
                <Typography variant="h6">{data ? "Edit" : "Add"} Site</Typography>
                {error && <Typography color="error">{error}</Typography>}
                <TextField
                    label="Title"
                    fullWidth
                    sx={{marginBottom: 2}}
                    name="name"
                    value={formData?.name || ''}
                    required={true}
                    onChange={handleChange}
                />
                <TextField
                    label="URL"
                    fullWidth
                    sx={{marginBottom: 2}}
                    name="baseURL"
                    required={true}
                    value={formData?.baseURL || ''}
                    onChange={handleChange}
                    helperText={formData?.baseURL && !isURLValid(formData.baseURL) ? "URL must start with http:// or https://" : ""}
                    error={!!(formData?.baseURL && !isURLValid(formData.baseURL))}
                />
                <Button variant="contained" sx={{marginBottom: 2}}
                        disabled={!formData?.baseURL || !isURLValid(formData.baseURL)}
                        onClick={() => findSitemapByBaseURL(formData?.baseURL)}>Find Sitemap</Button>
                <TextField
                    label="Enter Sitemap-URL manually"
                    multiline
                    fullWidth
                    sx={{marginBottom: 2}}
                    name="sitemap"
                    required={true}
                    value={(formData?.sitemap || '')}
                    onChange={(e) => setFormData({
                        ...formData,
                        sitemap: e.target.value
                    } as Site)}
                />

                <Box>
                    {data &&
                        <Button variant="contained" color="error" sx={{marginRight: 2}}
                                onClick={() => handleDeleteSite(data?.id)}>Delete</Button>
                    }
                    <Button variant="contained" color="secondary" sx={{marginRight: 2}}
                            onClick={handleAbortForm}>Cancel</Button>
                    <Button variant="contained" color="primary"
                            onClick={handleSubmit}
                            disabled={!isFormValid}>{data ? "Save" : "Add"}</Button>
                </Box>
            </CardContent>
        </Card>
    );
};

export default SiteForm;
