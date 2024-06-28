import axios from 'axios';
import {Site} from './types/Site';

export const fetchSitemap = async (url: string | undefined): Promise<string> => {
    if (!url) throw new Error("No URL provided.");
    const response = await axios.get<string>('/api/sitemaps/find', {params: {baseURL: url}});
    return response.data;
};

export const createSite = async (site: Site | undefined | null): Promise<Site> => {
    if (!site) throw new Error("Site data is null or undefined");
    const response = await axios.post<Site>('/api/sites', site);
    return response.data;
};

export const updateSite = async (site: Site | undefined | null): Promise<Site> => {
    if (!site) throw new Error("Site data is null or undefined");
    const response = await axios.put<Site>(`/api/sites/${site?.id}`, site);
    return response.data;
};

export const deleteSite = async (id: string | undefined): Promise<void> => {
    if (!id) throw new Error("Site ID is null or undefined");
    await axios.delete(`/api/sites/${id}`);
};
