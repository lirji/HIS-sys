import { Layout, Menu } from 'antd';
import { MedicineBoxFilled } from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { useThemeStore } from '../store/theme';
import { useAuth } from '../store/auth';
import { MENUS } from '../menu';

const { Sider } = Layout;

export default function AppSider() {
  const navigate = useNavigate();
  const location = useLocation();
  const collapsed = useThemeStore((s) => s.collapsed);
  const primary = useThemeStore((s) => s.primary);
  const { hasAnyRole } = useAuth();

  const items = MENUS.filter((m) => hasAnyRole(m.roles)).map((m) => ({
    key: m.path,
    icon: m.icon,
    label: m.label,
  }));

  return (
    <Sider
      width={224}
      collapsedWidth={72}
      collapsed={collapsed}
      theme="light"
      style={{
        borderInlineEnd: '1px solid rgba(128,128,128,0.12)',
        overflow: 'auto',
        height: '100vh',
        position: 'sticky',
        top: 0,
      }}
    >
      <div
        style={{
          height: 56,
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'flex-start',
          gap: 10,
          paddingInline: collapsed ? 0 : 18,
          fontWeight: 700,
          fontSize: 17,
          letterSpacing: 0.5,
          whiteSpace: 'nowrap',
          overflow: 'hidden',
        }}
      >
        <span
          style={{
            width: 30,
            height: 30,
            borderRadius: 9,
            background: `linear-gradient(135deg, ${primary}, ${primary}99)`,
            color: '#fff',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
            boxShadow: `0 4px 10px ${primary}55`,
          }}
        >
          <MedicineBoxFilled />
        </span>
        {!collapsed && <span>HIS 平台</span>}
      </div>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        items={items}
        onClick={({ key }) => navigate(key)}
        style={{ borderInlineEnd: 'none', paddingBlock: 8 }}
      />
    </Sider>
  );
}
