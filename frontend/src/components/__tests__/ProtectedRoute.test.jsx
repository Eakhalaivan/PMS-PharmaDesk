import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ProtectedRoute from '../auth/ProtectedRoute';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import * as AuthContext from '../../context/AuthContext';

describe('ProtectedRoute', () => {
  it('redirects to login when unauthenticated', () => {
    // Mock unauthenticated state
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: null,
      loading: false
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route path="/protected" element={<ProtectedRoute><div data-testid="protected-content">Protected Content</div></ProtectedRoute>} />
          <Route path="/login" element={<div data-testid="login-page">Login Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    // Initial navigation to /protected should redirect to /login
    expect(screen.getByTestId('login-page')).toBeInTheDocument();
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
  });

  it('renders children when authenticated', () => {
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: { id: 1, username: 'testuser', roles: ['PHARMACY_STAFF'] },
      loading: false
    });

    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
  });
});
