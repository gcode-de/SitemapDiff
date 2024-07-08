import {fireEvent, render, screen} from '@testing-library/react';
import Footer from './Footer';

describe('Footer Component', () => {
    const setIsAddSite = jest.fn();
    const handleCrawlAllSites = jest.fn();

    beforeEach(() => {
        setIsAddSite.mockClear();
        handleCrawlAllSites.mockClear();
    });

    test('renders Footer with buttons', () => {
        render(<Footer setIsAddSite={setIsAddSite} handleCrawlAllSites={handleCrawlAllSites}
                       editSiteId={null}/>);

        const addSiteButton = screen.getByText('Add Site');
        const crawlAllButton = screen.getByText('Crawl All');

        expect(addSiteButton).toBeInTheDocument();
        expect(crawlAllButton).toBeInTheDocument();
    });

    test('calls setIsAddSite when Add Site button is clicked', () => {
        render(<Footer setIsAddSite={setIsAddSite} handleCrawlAllSites={handleCrawlAllSites}
                       editSiteId={null}/>);

        const addSiteButton = screen.getByText('Add Site');
        fireEvent.click(addSiteButton);

        expect(setIsAddSite).toHaveBeenCalledTimes(1);
        expect(setIsAddSite).toHaveBeenCalledWith(true);
    });

    test('calls handleCrawlAllSites when Crawl All button is clicked', () => {
        render(<Footer setIsAddSite={setIsAddSite} handleCrawlAllSites={handleCrawlAllSites}
                       editSiteId={null}/>);

        const crawlAllButton = screen.getByText('Crawl All');
        fireEvent.click(crawlAllButton);

        expect(handleCrawlAllSites).toHaveBeenCalledTimes(1);
    });
});
