import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider, App as AntdApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import 'dayjs/locale/zh-cn';
import App from './App';
import './styles/global.css';
import { useThemeStore } from './store/theme';
import { buildTheme } from './theme';
import { IS_IDP } from './auth/config';
import OidcAuthProvider from './auth/OidcAuthProvider';

// 顶层订阅主题 store:模式/主色变化即时重算 antd 主题。
function Root() {
  const mode = useThemeStore((s) => s.mode);
  const primary = useThemeStore((s) => s.primary);

  // 同步给 <html>,供全局 css 区分亮暗(滚动条等)
  React.useEffect(() => {
    document.documentElement.dataset.theme = mode;
    document.documentElement.style.colorScheme = mode;
  }, [mode]);

  return (
    <ConfigProvider locale={zhCN} theme={buildTheme(mode, primary)}>
      <AntdApp style={{ height: '100%' }}>
        <BrowserRouter>
          {IS_IDP ? (
            <OidcAuthProvider>
              <App />
            </OidcAuthProvider>
          ) : (
            <App />
          )}
        </BrowserRouter>
      </AntdApp>
    </ConfigProvider>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>,
);
