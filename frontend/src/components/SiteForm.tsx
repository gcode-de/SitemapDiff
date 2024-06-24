import React, {ChangeEvent, useEffect, useState} from 'react';
import {Box, Button, Card, CardContent, TextField, Typography} from '@mui/material';
import axios from 'axios';
import {Site} from '../types/Site';

type SiteFormProps = {
    handleAbortForm: () => void,
    data: Site | null | undefined,
    refreshSites: () => void,
    handleAddSite: (site: Site | undefined | null) => void,
    handleEditSite: (site: Site) => void,
    handleDeleteSite: (id: string) => void,
}

const SiteForm: React.FC<SiteFormProps> = ({
                                               handleAbortForm,
                                               data,
                                               refreshSites
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
        if (formData?.name && formData?.baseURL && formData?.sitemaps && formData.sitemaps.length > 0) {
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

    const findSitemapsByBaseURL = async (url: string | undefined): Promise<string[]> => {
        if (!url) throw new Error("No URL provided.");
        if (!isURLValid(url)) {
            setError("URL must start with http:// or https://");
            return [];
        }

        try {
            const response = await axios.get<string[]>('/api/sitemaps/find', {params: {baseURL: url}});
            setFormData({...formData, sitemaps: response.data} as Site)
            setError(null);
            return response.data;
        } catch (error: any) {
            setError(error.response ? error.response.data : "Error finding sitemaps");
            throw new Error("Could not retrieve sitemaps.");
        }
    };


    const createSite = async (site: Site | undefined | null) => {
        try {
            const response = await axios.post<Site>('/api/sites', site);
            refreshSites();
            handleAbortForm();
        } catch (error) {
            console.error('Error creating site:', error);
        }
    };

    const updateSite = async (site: Site | undefined | null) => {
        try {
            const response = await axios.put<Site>(`/api/sites/${site?.id}`, site);
            refreshSites();
            handleAbortForm();
        } catch (error) {
            console.error('Error updating site:', error);
        }
    };

    const deleteSite = async (id: string | undefined) => {
        try {
            if (window.confirm('Are you sure you want to delete this site?')) {
                await axios.delete(`/api/sites/${id}`);
                refreshSites();
                handleAbortForm();
            }
        } catch (error) {
            console.error('Error deleting site:', error);
        }
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
                        onClick={() => findSitemapsByBaseURL(formData?.baseURL)}>Find
                    Sitemaps</Button>
                <TextField
                    label="Enter Sitemap-URLs manually, one per line"
                    multiline
                    rows={4}
                    fullWidth
                    sx={{marginBottom: 2}}
                    name="sitemaps"
                    required={true}
                    value={(formData?.sitemaps || []).join('\n')}
                    onChange={(e) => setFormData({
                        ...formData,
                        sitemaps: e.target.value.split('\n')
                    } as Site)}
                />

                <Box>
                    {data &&
                        <Button variant="contained" color="error" sx={{marginRight: 2}}
                                onClick={() => deleteSite(data?.id)}>Delete</Button>
                    }
                    <Button variant="contained" color="secondary" sx={{marginRight: 2}}
                            onClick={handleAbortForm}>Cancel</Button>
                    {data ?
                        <Button variant="contained" color="primary"
                                onClick={() => updateSite(formData)}
                                disabled={!isFormValid}>Save</Button> :
                        <Button variant="contained" color="primary"
                                onClick={() => createSite(formData)}
                                disabled={!isFormValid}>Add</Button>
                    }
                </Box>
            </CardContent>
        </Card>
    );
};

export default SiteForm;
