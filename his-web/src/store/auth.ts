import { create } from 'zustand';
import { TOKEN_KEY } from '../api/client';
import type { LoginResponse } from '../api/types';

const USER_KEY = 'his_user';

export interface AuthState {
  token: string | null;
  user: Omit<LoginResponse, 'token' | 'expiresIn'> | null;
  setSession: (resp: LoginResponse) => void;
  logout: () => void;
  clearSession: () => void;
  hasAnyRole: (roles: string[]) => boolean;
}

function loadUser(): AuthState['user'] {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export const useAuth = create<AuthState>((set, get) => ({
  token: localStorage.getItem(TOKEN_KEY),
  user: loadUser(),
  setSession: (resp) => {
    const { token, expiresIn, ...user } = resp;
    void expiresIn;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    set({ token, user });
  },
  logout: () => {
    localStorage.clear();
    set({ token: null, user: null });
  },
  // 只清应用会话键(保留 oidc-client 自身在 localStorage 的登录态),供 idp 模式桥接使用
  clearSession: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    set({ token: null, user: null });
  },
  hasAnyRole: (roles) => {
    if (roles.length === 0) return true;
    const mine = get().user?.roles ?? [];
    return roles.some((r) => mine.includes(r));
  },
}));
