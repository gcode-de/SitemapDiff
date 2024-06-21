import {fireEvent, render, screen} from '@testing-library/react';
import Home from './Home';
import '@testing-library/jest-dom';
import {Site} from '../types/Site';

describe('Home Component', () => {
    const sites: Site[] = [
        {
            id: '1',
            name: 'Site 1',
            baseURL: 'https://site1.com',
            sitemaps: [],
            userId: 'user1',
            scrapeCron: '',
            crawls: []
        },
        {
            id: '2',
            name: 'Site 2',
            baseURL: 'https://site2.com',
            sitemaps: [],
            userId: 'user2',
            scrapeCron: '',
            crawls: []
        }
    ];

    test('renders Home with SiteList', () => {
        render(<Home sites={sites}/>);
        const siteList = screen.getByText('Site 1');
        expect(siteList).toBeInTheDocument();
    });


    test('calls handleCrawlAllSites when Crawl All button is clicked', () => {
        render(<Home sites={sites}/>);

        const crawlAllButton = screen.getByRole('button', {name: /crawl all/i});
        fireEvent.click(crawlAllButton);
    });
});