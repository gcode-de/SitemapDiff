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

    // test('displays SiteForm when Add Site button is clicked', () => {
    //     render(<Home sites={sites}/>);
    //
    //     // Ensure the Add Site button is in the document
    //     const addButton = screen.getByRole('button', {name: /add site/i});
    //     fireEvent.click(addButton);
    //
    //     const siteForm = screen.getByText('Add Site');
    //     expect(siteForm).toBeInTheDocument();
    // });
    //
    // test('calls handleAddSite when Add button in form is clicked', () => {
    //     render(<Home sites={sites}/>);
    //
    //     const addButton = screen.getByRole('button', {name: /add site/i});
    //     fireEvent.click(addButton);
    //
    //     const siteForm = screen.getByText('Add Site');
    //     expect(siteForm).toBeInTheDocument();
    //
    //     // Simulate filling out and submitting the form
    //     const titleInput = screen.getByLabelText('Title');
    //     const urlInput = screen.getByLabelText('URL');
    //     fireEvent.change(titleInput, {target: {value: 'New Site'}});
    //     fireEvent.change(urlInput, {target: {value: 'https://newsite.com'}});
    //
    //     const saveButton = screen.getByRole('button', {name: /^add$/i});
    //     fireEvent.click(saveButton);
    //
    //     // Check console log for added site
    //     // You might want to spy on console.log or use a mock function
    // });
    //
    // test('calls handleEditSite when Edit button is clicked', () => {
    //     render(<Home sites={sites}/>);
    //
    //     const editButtons = screen.getAllByRole('button', {name: /edit site/i});
    //     expect(editButtons.length).toBeGreaterThan(0);
    //
    //     const editButton = editButtons[0]; // Select the first "Edit Site" button
    //     fireEvent.click(editButton);
    //
    //     const siteForm = screen.getByText('Edit Site');
    //     expect(siteForm).toBeInTheDocument();
    //
    //     // Simulate filling out and submitting the form
    //     const titleInput = screen.getByLabelText('Title');
    //     const urlInput = screen.getByLabelText('URL');
    //     fireEvent.change(titleInput, {target: {value: 'Updated Site'}});
    //     fireEvent.change(urlInput, {target: {value: 'https://updatedsite.com'}});
    //
    //     const saveButton = screen.getByRole('button', {name: /^save$/i});
    //     fireEvent.click(saveButton);
    //
    //     // Check console log for edited site
    //     // You might want to spy on console.log or use a mock function
    // });
    //
    // test('calls handleDeleteSite when Delete button is clicked', () => {
    //     render(<Home sites={sites}/>);
    //
    //     const deleteButtons = screen.getAllByRole('button', {name: "Delete"});
    //     expect(deleteButtons.length).toBeGreaterThan(0);
    //
    //     const deleteButton = deleteButtons[0];
    //     fireEvent.click(deleteButton);
    //
    //     // Check console log for deleted site
    //     // You might want to spy on console.log or use a mock function
    // });

    test('calls handleCrawlAllSites when Crawl All button is clicked', () => {
        render(<Home sites={sites}/>);

        const crawlAllButton = screen.getByRole('button', {name: /crawl all/i});
        fireEvent.click(crawlAllButton);

        // Check console log for crawl all sites
        // You might want to spy on console.log or use a mock function
    });
});
