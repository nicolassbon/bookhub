import { SessionStore } from './session.store';
import { UserResponse } from '../api/contracts/auth';

describe('SessionStore', () => {
  let store: SessionStore;

  const mockUser: UserResponse = {
    id: 'user-1',
    email: 'test@bookhub.local',
    displayName: 'Tester',
    role: 'ROLE_USER',
    createdAt: '2026-07-23T20:00:00Z'
  };

  beforeEach(() => {
    store = new SessionStore();
  });

  it('should start unauthenticated', () => {
    expect(store.isAuthenticated()).toBe(false);
    expect(store.user()).toBeNull();
    expect(store.accessToken()).toBeNull();
  });

  it('should set session on login', () => {
    store.setSession('jwt-token-123', mockUser);

    expect(store.isAuthenticated()).toBe(true);
    expect(store.accessToken()).toBe('jwt-token-123');
    expect(store.user()).toEqual(mockUser);
  });

  it('should clear session on logout', () => {
    store.setSession('jwt-token-123', mockUser);
    store.clearSession();

    expect(store.isAuthenticated()).toBe(false);
    expect(store.accessToken()).toBeNull();
    expect(store.user()).toBeNull();
  });
});
