import { Tag, Dropdown, Button, theme } from 'antd';
import { CloseOutlined, DownOutlined } from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTabsStore } from '../store/tabs';

export default function TabBar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = theme.useToken();
  const { tabs, removeTab, removeOthers, removeAll } = useTabsStore();

  const active = location.pathname;

  const closeAndRedirect = (path: string) => {
    // 关闭当前页签时跳到右邻或首页
    if (path === active) {
      const idx = tabs.findIndex((t) => t.path === path);
      const next = tabs[idx + 1] ?? tabs[idx - 1];
      navigate(next ? next.path : '/');
    }
    removeTab(path);
  };

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        padding: '8px 12px',
        background: token.colorBgContainer,
        borderBottom: `1px solid ${token.colorBorderSecondary}`,
        overflowX: 'auto',
        whiteSpace: 'nowrap',
      }}
    >
      <div style={{ flex: 1, display: 'flex', gap: 8 }}>
        {tabs.map((t) => {
          const selected = t.path === active;
          return (
            <Tag
              key={t.path}
              color={selected ? token.colorPrimary : undefined}
              onClick={() => navigate(t.path)}
              style={{
                cursor: 'pointer',
                paddingInline: 12,
                paddingBlock: 4,
                borderRadius: 6,
                margin: 0,
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                userSelect: 'none',
              }}
            >
              {t.label}
              {t.path !== '/' && (
                <CloseOutlined
                  style={{ fontSize: 10 }}
                  onClick={(e) => {
                    e.stopPropagation();
                    closeAndRedirect(t.path);
                  }}
                />
              )}
            </Tag>
          );
        })}
      </div>
      <Dropdown
        menu={{
          items: [
            { key: 'others', label: '关闭其他', onClick: () => removeOthers(active) },
            { key: 'all', label: '关闭全部', onClick: () => { removeAll(); navigate('/'); } },
          ],
        }}
      >
        <Button type="text" size="small" icon={<DownOutlined />} />
      </Dropdown>
    </div>
  );
}
