import { create } from 'zustand';

// 多页签:记录已访问路由。工作台('/')为固定首页,不可关闭。
export interface TabItem {
  path: string;
  label: string;
}

const HOME: TabItem = { path: '/', label: '工作台' };

interface TabsState {
  tabs: TabItem[];
  addTab: (t: TabItem) => void;
  removeTab: (path: string) => void;
  removeOthers: (path: string) => void;
  removeAll: () => void;
}

export const useTabsStore = create<TabsState>((set) => ({
  tabs: [HOME],
  addTab: (t) =>
    set((s) => (s.tabs.some((x) => x.path === t.path) ? s : { tabs: [...s.tabs, t] })),
  removeTab: (path) =>
    set((s) => ({ tabs: s.tabs.filter((x) => x.path === '/' || x.path !== path) })),
  removeOthers: (path) =>
    set((s) => ({ tabs: s.tabs.filter((x) => x.path === '/' || x.path === path) })),
  removeAll: () => set({ tabs: [HOME] }),
}));
