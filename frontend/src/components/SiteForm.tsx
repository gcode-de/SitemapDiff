import React, {ChangeEvent, useEffect, useState} from 'react';
import {Box, Button, Card, CardContent, TextField, Typography} from '@mui/material';
import {Site} from "../types/Site.tsx";

type SiteFormProps = {
    handleEditSite: (formData: Site | undefined | null) => Site,
    handleAddSite: (formData: Site | undefined | null) => Site,
    handleDeleteSite: (id: string | undefined) => string;
    handleAbortForm: () => void,
    data: Site | null | undefined,
}

const SiteForm: React.FC<SiteFormProps> = ({
                                               handleEditSite,
                                               handleAddSite,
                                               handleDeleteSite,
                                               handleAbortForm,
                                               data
                                           }: SiteFormProps) => {

    const [formData, setFormData] = useState<Site | undefined | null>(data);
    const [isFormValid, setIsFormValid] = useState<boolean>(false);

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

    const findSitemapsByBaseURL = (url: string | undefined): string[] => {
        if (!url) throw new Error("No URL provided.")
        console.log("Find Sitemaps for", url)
        return [url]
    }

    function checkSitemaps(sitemaps: string[] | undefined) {
        if (!sitemaps) throw new Error("No sitemaps provided.")
        console.log("Check Sitemaps:", sitemaps)
        return [sitemaps]
    }

    return (
        <Card key={'siteForm'} sx={{width: '360px'}}>
            <CardContent>
                <Typography variant="h6">{data ? "Edit" : "Add"} Site</Typography>
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
                />
                <Button variant="contained" sx={{marginBottom: 2}}
                        disabled={!formData?.baseURL} onClick={() => findSitemapsByBaseURL(formData?.baseURL)}>Find
                    Sitemaps</Button>
                <TextField
                    label="Enter Sitemap-URLs manually one per line"
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
                <Button variant="contained" sx={{marginBottom: 2}} disabled={!formData?.sitemaps}
                        onClick={() => checkSitemaps(formData?.sitemaps)}>Check Sitemaps</Button>
                <Box>
                    {data &&
                        <Button variant="contained" color="error" sx={{marginRight: 2}} onClick={() => {
                            handleDeleteSite(data?.id)
                        }}>Delete</Button>
                    }
                    <Button variant="contained" color="secondary" sx={{marginRight: 2}}
                            onClick={handleAbortForm}>Cancel</Button>
                    {data ?
                        <Button variant="contained" color="primary"
                                onClick={() => handleEditSite(formData)}
                                disabled={!isFormValid}>Save</Button> :
                        <Button variant="contained" color="primary"
                                onClick={() => handleAddSite(formData)}
                                disabled={!isFormValid}>Add</Button>
                    }
                </Box>
            </CardContent>
        </Card>
    );
};

export default SiteForm;
