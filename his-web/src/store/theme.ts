import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { DEFAULT_PRIMARY, type ThemeMode } from '../theme';

// 主题偏好:模式(亮/暗)、主色、侧栏折叠。持久化到 localStorage,刷新保留。
interface ThemeState {
  mode: ThemeMode;
  primary: string;
  collapsed: boolean;
  toggleMode: () => void;
  setPrimary: (c: string) => void;
  toggleCollapsed: () => void;
  setCollapsed: (v: boolean) => void;
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set) => ({
      mode: 'light',
      primary: DEFAULT_PRIMARY,
      collapsed: false,
      toggleMode: () => set((s) => ({ mode: s.mode === 'light' ? 'dark' : 'light' })),
      setPrimary: (primary) => set({ primary }),
      toggleCollapsed: () => set((s) => ({ collapsed: !s.collapsed })),
      setCollapsed: (collapsed) => set({ collapsed }),
    }),
    { name: 'his_theme' },
  ),
);
