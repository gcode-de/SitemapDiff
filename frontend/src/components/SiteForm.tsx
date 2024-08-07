import React, {ChangeEvent, useEffect, useState} from 'react';
import {
    Box,
    Button,
    Card,
    CardContent,
    TextField,
    Typography,
    MenuItem,
    Select,
    FormControl,
    InputLabel, SelectChangeEvent, Divider
} from '@mui/material';
import {Site} from '../types/Site';
import {fetchSitemap} from '../api';

type SiteFormProps = {
    handleAbortForm: () => void,
    data: Site | null | undefined,
    refreshSites: () => void,
    handleAddSite: (site: Site | undefined | null) => void,
    handleEditSite: (site: Site | undefined | null) => void,
    handleDeleteSite: (id: string) => void,
    userMail: string | undefined,
}

const SiteForm: React.FC<SiteFormProps> = ({
                                               handleAbortForm,
                                               data,
                                               refreshSites,
                                               handleAddSite,
                                               handleEditSite,
                                               handleDeleteSite,
                                               userMail
                                           }: SiteFormProps) => {

    const [formData, setFormData] = useState<Site | undefined | null>(data);
    const [isFormValid, setIsFormValid] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!data) {
            setFormData(prevFormData => ({
                ...prevFormData,
                email: userMail || '',
            } as Site));
        }
    }, [data, userMail]);

    const extractSecondLevelDomain = (url: string): string => {
        try {
            if (!url.startsWith('http://') && !url.startsWith('https://')) {
                url = 'http://' + url;
            }
            const domain = new URL(url).hostname;
            const domainParts = domain.split('.');

            let extractedDomain = '';
            if (domainParts.length > 2) {
                extractedDomain = domainParts.slice(-2, -1)[0] !== 'www' ? domainParts.slice(-2, -1)[0] : domainParts.slice(-3, -2)[0];
            } else if (domainParts.length === 2) {
                extractedDomain = domainParts[0] !== 'www' ? domainParts[0] : '';
            }

            if (extractedDomain) {
                return extractedDomain.charAt(0).toUpperCase() + extractedDomain.slice(1);
            }
            return '';
        } catch (error) {
            return '';
        }
    };


    const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
        const {name, value} = e.target;
        setFormData((prevFormData) => {
            const updatedFormData = {
                ...prevFormData,
                [name as string]: value.trim(),
            } as Site;

            if (name === 'baseURL' && !updatedFormData.name) {
                const secondLevelDomain = extractSecondLevelDomain(value.trim());
                if (secondLevelDomain) {
                    updatedFormData.name = secondLevelDomain;
                }
            }

            return updatedFormData;
        });
    };

    useEffect(() => {
        if (formData?.name && formData?.baseURL && formData?.sitemap) {
            setIsFormValid(true);
        } else {
            setIsFormValid(false);
        }
    }, [formData]);

    const findSitemapByBaseURL = async (url: string | undefined): Promise<string> => {
        if (!url) throw new Error("No URL provided.");

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
            handleEditSite(formData);
        } else {
            handleAddSite(formData);
        }
        refreshSites();
        handleAbortForm();
    };

    const handleSuggestionClick = () => {
        setFormData((prevFormData) => ({
            ...prevFormData,
            email: userMail,
        } as Site));
    };

    return (
        <Card key={'siteForm'} sx={{width: '360px'}}>
            <CardContent>
                <Typography variant="h5" sx={{marginBottom: 3}}>{data ? "Edit" : "Add"} Site</Typography>
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
                />
                <Button variant="contained" sx={{marginBottom: 2}}
                        disabled={!formData?.baseURL}
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
                        sitemap: e.target.value.trim()
                    } as Site)}
                />
                <Divider sx={{marginBottom: 2}}/>
                <TextField
                    label="Email to receive scheduled crawl results"
                    fullWidth
                    sx={{marginBottom: 2}}
                    name="email"
                    value={formData?.email === null ? userMail : formData?.email || ''}
                    onChange={handleChange}
                />
                {userMail && formData?.email !== userMail && (
                    <Typography
                        variant="body2"
                        sx={{cursor: 'pointer', textDecoration: 'underline', marginTop: '-10px', marginBottom: '10px'}}
                        onClick={handleSuggestionClick}
                    >
                        Use {userMail}
                    </Typography>
                )}
                <FormControl fullWidth sx={{marginBottom: 2}}>
                    <InputLabel id="crawlSchedule-label">Crawl Schedule</InputLabel>
                    <Select
                        labelId="crawlSchedule-label"
                        name="crawlSchedule"
                        value={formData?.crawlSchedule || 'never'}
                        onChange={handleChange}
                        label="Crawl Schedule"
                    >
                        <MenuItem value="never"><em>Never</em></MenuItem>
                        <MenuItem value="daily">Daily</MenuItem>
                        <MenuItem value="weekly">Weekly</MenuItem>
                        <MenuItem value="monthly">Monthly</MenuItem>
                    </Select>
                </FormControl>

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
