import {fireEvent, render, screen} from '@testing-library/react';
import SiteForm from './SiteForm';
import {Site} from '../types/Site';

const mockHandleEditSite = jest.fn();
const mockHandleAddSite = jest.fn();
const mockHandleDeleteSite = jest.fn();
const mockHandleAbortForm = jest.fn();

const defaultProps = {
    handleEditSite: mockHandleEditSite,
    handleAddSite: mockHandleAddSite,
    handleDeleteSite: mockHandleDeleteSite,
    handleAbortForm: mockHandleAbortForm,
    refreshSites: jest.fn(),
    data: null,
    userMail: "",
};


describe('SiteForm Component', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    test('renders Add Site form', () => {
        render(<SiteForm {...defaultProps} />);

        expect(screen.getByText(/Add Site/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Title/i)).toBeInTheDocument();
        expect(screen.getByRole('textbox', {name: 'URL'})).toBeInTheDocument();
        expect(screen.getByLabelText(/Enter Sitemap-URL manually/i)).toBeInTheDocument();
    });


    test('renders Edit Site form', () => {
        const data: Site = {
            id: '1',
            name: 'Test Site',
            baseURL: 'https://testsite.com',
            sitemap: 'https://testsite.com/sitemap.xml',
            favicon: '',
            userId: 'user1',
            crawlSchedule: "",
            email: "",
            crawls: []
        };

        render(<SiteForm {...defaultProps} data={data}/>);

        expect(screen.getByText(/Edit Site/i)).toBeInTheDocument();
        expect(screen.getByDisplayValue('Test Site')).toBeInTheDocument();
        expect(screen.getByDisplayValue('https://testsite.com')).toBeInTheDocument();
        expect(screen.getByDisplayValue('https://testsite.com/sitemap.xml')).toBeInTheDocument();
    });

    test('enables Add button when form is valid', () => {
        render(<SiteForm {...defaultProps} />);

        fireEvent.change(screen.getByLabelText(/Title/i), {target: {value: 'New Site'}});
        fireEvent.change(screen.getAllByLabelText(/URL/i)[0], {target: {value: 'https://newsite.com'}});
        fireEvent.change(screen.getByLabelText(/Enter Sitemap-URL manually/i), {target: {value: 'https://newsite.com/sitemap.xml'}});

        expect(screen.getByRole('button', {name: /add/i})).toBeEnabled();
    });

    test('disables Add button when form is invalid', () => {
        render(<SiteForm {...defaultProps} />);

        fireEvent.change(screen.getByLabelText(/Title/i), {target: {value: ''}});
        fireEvent.change(screen.getAllByLabelText(/URL/i)[0], {target: {value: ''}});
        fireEvent.change(screen.getByLabelText(/Enter Sitemap-URL manually/i), {target: {value: ''}});

        expect(screen.getByRole('button', {name: /add/i})).toBeDisabled();
    });

    test('calls handleAddSite when Add button is clicked', () => {
        render(<SiteForm {...defaultProps} />);

        fireEvent.change(screen.getByLabelText(/Title/i), {target: {value: 'New Site'}});
        fireEvent.change(screen.getAllByLabelText(/URL/i)[0], {target: {value: 'https://newsite.com'}});
        fireEvent.change(screen.getByLabelText(/Enter Sitemap-URL manually/i), {target: {value: 'https://newsite.com/sitemap.xml'}});

        fireEvent.click(screen.getByRole('button', {name: /add/i}));

        expect(mockHandleAddSite).toHaveBeenCalled();
    });

    test('calls handleEditSite when Save button is clicked', () => {
        const data: Site = {
            id: '1',
            name: 'Test Site',
            baseURL: 'https://testsite.com',
            sitemap: 'https://testsite.com/sitemap.xml',
            favicon: '',
            userId: 'user1',
            crawlSchedule: "",
            email: "",
            crawls: []
        };

        render(<SiteForm {...defaultProps} data={data}/>);

        fireEvent.change(screen.getByLabelText(/Title/i), {target: {value: 'Updated Site'}});
        fireEvent.change(screen.getAllByLabelText(/URL/i)[0], {target: {value: 'https://updatedsite.com'}});

        fireEvent.click(screen.getByRole('button', {name: /save/i}));

        expect(mockHandleEditSite).toHaveBeenCalled();
    });

    test('calls handleDeleteSite when Delete button is clicked', () => {
        const data: Site = {
            id: '1',
            name: 'Test Site',
            baseURL: 'https://testsite.com',
            sitemap: 'https://testsite.com/sitemap.xml',
            favicon: '',
            userId: 'user1',
            crawlSchedule: "",
            email: "",
            crawls: []
        };

        render(<SiteForm {...defaultProps} data={data}/>);

        fireEvent.click(screen.getByRole('button', {name: /delete/i}));

        expect(mockHandleDeleteSite).toHaveBeenCalled();
    });

    test('calls handleAbortForm when Cancel button is clicked', () => {
        render(<SiteForm {...defaultProps} />);

        fireEvent.click(screen.getByRole('button', {name: /cancel/i}));

        expect(mockHandleAbortForm).toHaveBeenCalled();
    });

    test('disables Find Sitemap button if URL is empty', () => {
        render(<SiteForm {...defaultProps} />);

        expect(screen.getByRole('button', {name: /find sitemap/i})).toBeDisabled();
    });

    test('enables Find Sitemap button if URL is provided', () => {
        render(<SiteForm {...defaultProps} />);

        fireEvent.change(screen.getAllByLabelText(/URL/i)[0], {target: {value: 'https://newsite.com'}});

        expect(screen.getByRole('button', {name: /find sitemap/i})).toBeEnabled();
    });

});
