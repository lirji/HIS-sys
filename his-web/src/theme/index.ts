import { theme as antdTheme, type ThemeConfig } from 'antd';

// 主题模式与主色集中配置。Soybean 风格:圆角更大、控件更高、卡片阴影柔和、
// 亮/暗双算法。组件级 token 让侧栏/顶栏/卡片观感统一。

export type ThemeMode = 'light' | 'dark';

// 可选主色(主题抽屉切换用)。默认取医疗场景偏稳重的蓝。
export const PRESET_COLORS: { key: string; color: string }[] = [
  { key: '科技蓝', color: '#2563eb' },
  { key: '极客青', color: '#13c2c2' },
  { key: '薄荷绿', color: '#10b981' },
  { key: '靛蓝', color: '#4f46e5' },
  { key: '品红', color: '#eb2f96' },
  { key: '日落橙', color: '#f5701a' },
];

export const DEFAULT_PRIMARY = PRESET_COLORS[0].color;

export function buildTheme(mode: ThemeMode, primary: string): ThemeConfig {
  const dark = mode === 'dark';
  return {
    algorithm: dark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
    token: {
      colorPrimary: primary,
      colorInfo: primary,
      borderRadius: 8,
      borderRadiusLG: 12,
      wireframe: false,
      fontSize: 14,
      controlHeight: 36,
      colorBgLayout: dark ? '#101014' : '#f4f6fb',
    },
    components: {
      Layout: {
        headerBg: dark ? '#1a1a20' : '#ffffff',
        headerHeight: 56,
        headerPadding: '0 16px',
        siderBg: dark ? '#16161c' : '#ffffff',
        bodyBg: dark ? '#101014' : '#f4f6fb',
      },
      Menu: {
        itemBorderRadius: 8,
        itemMarginInline: 8,
        itemHeight: 42,
        activeBarWidth: 0,
        itemSelectedBg: dark ? 'rgba(255,255,255,0.08)' : `${primary}1f`,
        itemSelectedColor: primary,
      },
      Card: {
        borderRadiusLG: 14,
        boxShadowTertiary: dark
          ? '0 1px 2px rgba(0,0,0,0.45)'
          : '0 1px 2px rgba(0,0,0,0.04), 0 4px 16px rgba(15,23,42,0.06)',
      },
      Button: {
        borderRadius: 8,
        controlHeight: 36,
        primaryShadow: 'none',
      },
      Table: {
        borderRadiusLG: 12,
        headerBg: dark ? '#1d1d24' : '#fafbfd',
        headerSplitColor: 'transparent',
      },
    },
  };
}
