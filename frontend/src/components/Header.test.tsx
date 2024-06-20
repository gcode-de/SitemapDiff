import {fireEvent, render, screen} from '@testing-library/react';
import Header from './Header';
import '@testing-library/jest-dom';
import {User} from '../types/User';

describe('Header Component', () => {
    const login = jest.fn();
    const logout = jest.fn();
    const user: User = {
        id: '1',
        name: 'John Doe',
        email: 'john@example.com',
        picture: 'http://example.com/picture.jpg'
    };

    beforeEach(() => {
        login.mockClear();
        logout.mockClear();
    });

    test('renders Header with title', () => {
        render(<Header login={login} logout={logout} user={null}/>);

        const title = screen.getByText('SitemapDiff');

        expect(title).toBeInTheDocument();
    });

    test('renders login button when user is not logged in', () => {
        render(<Header login={login} logout={logout} user={null}/>);

        const loginButton = screen.getByText('Login with');

        expect(loginButton).toBeInTheDocument();
    });

    test('renders user greeting and logout button when user is logged in', () => {
        render(<Header login={login} logout={logout} user={user}/>);

        const greeting = screen.getByText(`Hallo, ${user.name}!`);
        const logoutButton = screen.getByText('Logout');

        expect(greeting).toBeInTheDocument();
        expect(logoutButton).toBeInTheDocument();
    });

    test('calls login function when login button is clicked', () => {
        render(<Header login={login} logout={logout} user={null}/>);

        const loginButton = screen.getByText('Login with');
        fireEvent.click(loginButton);

        expect(login).toHaveBeenCalledTimes(1);
    });

    test('calls logout function when logout button is clicked', () => {
        render(<Header login={login} logout={logout} user={user}/>);

        const logoutButton = screen.getByText('Logout');
        fireEvent.click(logoutButton);

        expect(logout).toHaveBeenCalledTimes(1);
    });
});
