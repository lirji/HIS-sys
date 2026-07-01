import { useEffect } from 'react';
import { Layout, theme } from 'antd';
import { Outlet, useLocation } from 'react-router-dom';
import AppSider from './Sider';
import AppHeader from './Header';
import TabBar from './TabBar';
import { MENU_MAP } from '../menu';
import { useTabsStore } from '../store/tabs';

const { Content } = Layout;

export default function MainLayout() {
  const location = useLocation();
  const { token } = theme.useToken();
  const addTab = useTabsStore((s) => s.addTab);

  // 进入已知路由时登记页签
  useEffect(() => {
    const meta = MENU_MAP[location.pathname];
    if (meta) addTab({ path: meta.path, label: meta.label });
  }, [location.pathname, addTab]);

  return (
    <Layout style={{ minHeight: '100vh' }} hasSider>
      <AppSider />
      <Layout style={{ background: token.colorBgLayout }}>
        <AppHeader />
        <TabBar />
        <Content style={{ margin: 16, overflow: 'auto' }}>
          <div key={location.pathname} className="his-page-enter">
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
