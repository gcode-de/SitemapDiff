import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import Home from './Home';
import '@testing-library/jest-dom';
import {Site} from '../types/Site';
import axios from 'axios';
import {User} from "../types/User.tsx";

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('Home Component', () => {
    const sites: Site[] = [
        {
            id: '1',
            name: 'Site 1',
            baseURL: 'https://site1.com',
            sitemap: '',
            favicon: '',
            userId: 'user1',
            crawlSchedule: '',
            email: '',
            crawls: []
        },
        {
            id: '2',
            name: 'Site 2',
            baseURL: 'https://site2.com',
            sitemap: '',
            favicon: '',
            userId: 'user2',
            crawlSchedule: '',
            email: '',
            crawls: []
        }
    ];

    const user: User = {
        name: "string",
        email: "string",
        id: "string",
        picture: "string"
    }

    const refreshSitesMock = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders Home with SiteList', () => {
        render(<Home sites={sites} refreshSites={refreshSitesMock} user={user}/>);
        const siteList = screen.getByText('Site 1');
        expect(siteList).toBeInTheDocument();
    });

    test('calls handleCrawlAllSites when Crawl All button is clicked', async () => {
        mockedAxios.get.mockResolvedValue({data: []});

        render(<Home sites={sites} refreshSites={refreshSitesMock} user={user}/>);

        const crawlAllButton = screen.getByRole('button', {name: /crawl all/i});
        fireEvent.click(crawlAllButton);

        await waitFor(() => {
            expect(mockedAxios.get).toHaveBeenCalledWith('/api/crawls/start/1');
            expect(mockedAxios.get).toHaveBeenCalledWith('/api/crawls/start/2');
        });
    });

    test('handles network error', async () => {
        const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
        mockedAxios.get.mockRejectedValue(new Error('Network Error'));

        render(<Home sites={sites} refreshSites={refreshSitesMock} user={user}/>);

        const crawlAllButton = screen.getByRole('button', {name: /crawl all/i});
        fireEvent.click(crawlAllButton);

        await waitFor(() => {
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error crawling site:', expect.any(Error));
        });

        consoleErrorSpy.mockRestore();
    });
});
