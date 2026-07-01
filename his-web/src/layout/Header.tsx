import { useEffect, useState } from 'react';
import {
  Layout,
  Breadcrumb,
  Button,
  Dropdown,
  Badge,
  Avatar,
  Tag,
  Popover,
  Tooltip,
  List,
  Empty,
  Space,
  Typography,
  theme,
} from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  SunOutlined,
  MoonOutlined,
  BgColorsOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  CheckOutlined,
} from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { useThemeStore } from '../store/theme';
import { useAuth } from '../store/auth';
import { IS_IDP, userManager } from '../auth/config';
import { MENU_MAP } from '../menu';
import { PRESET_COLORS } from '../theme';
import { inbox, markAllRead, markRead, unreadCount } from '../api/endpoints';
import type { Notification } from '../api/types';

const { Header } = Layout;

export default function AppHeader() {
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = theme.useToken();
  const { user, logout } = useAuth();
  const { collapsed, toggleCollapsed, mode, toggleMode, primary, setPrimary } = useThemeStore();

  const [unread, setUnread] = useState(0);
  const [list, setList] = useState<Notification[]>([]);
  const [fullscreen, setFullscreen] = useState(false);

  const refreshUnread = () =>
    unreadCount()
      .then((r) => setUnread(r.count))
      .catch(() => void 0);

  useEffect(() => {
    refreshUnread();
    const t = setInterval(refreshUnread, 20000);
    return () => clearInterval(t);
  }, [location.pathname]);

  useEffect(() => {
    const onFs = () => setFullscreen(!!document.fullscreenElement);
    document.addEventListener('fullscreenchange', onFs);
    return () => document.removeEventListener('fullscreenchange', onFs);
  }, []);

  const toggleFullscreen = () => {
    if (document.fullscreenElement) document.exitFullscreen();
    else document.documentElement.requestFullscreen?.();
  };

  const openNotify = async (open: boolean) => {
    if (open) {
      try {
        setList(await inbox(true));
      } catch {
        /* handled */
      }
    }
  };

  const current = MENU_MAP[location.pathname];

  const colorPanel = (
    <Space size={10} style={{ padding: 4 }}>
      {PRESET_COLORS.map((c) => (
        <Tooltip title={c.key} key={c.color}>
          <span
            onClick={() => setPrimary(c.color)}
            style={{
              width: 22,
              height: 22,
              borderRadius: 6,
              background: c.color,
              cursor: 'pointer',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              boxShadow: primary === c.color ? `0 0 0 2px #fff, 0 0 0 4px ${c.color}` : 'none',
            }}
          >
            {primary === c.color && <CheckOutlined style={{ fontSize: 12 }} />}
          </span>
        </Tooltip>
      ))}
    </Space>
  );

  const notifyPanel = (
    <div style={{ width: 320 }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 8,
        }}
      >
        <Typography.Text strong>未读通知</Typography.Text>
        <Button
          type="link"
          size="small"
          disabled={unread === 0}
          onClick={async () => {
            await markAllRead();
            setList([]);
            refreshUnread();
          }}
        >
          全部已读
        </Button>
      </div>
      {list.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无未读" />
      ) : (
        <List
          size="small"
          dataSource={list.slice(0, 6)}
          style={{ maxHeight: 320, overflow: 'auto' }}
          renderItem={(n) => (
            <List.Item
              style={{ cursor: 'pointer', paddingInline: 4 }}
              onClick={async () => {
                await markRead(n.id);
                setList((p) => p.filter((x) => x.id !== n.id));
                refreshUnread();
                navigate('/notifications');
              }}
            >
              <List.Item.Meta
                title={<span style={{ fontSize: 13 }}>{n.title}</span>}
                description={
                  <Typography.Paragraph
                    type="secondary"
                    ellipsis={{ rows: 1 }}
                    style={{ margin: 0, fontSize: 12 }}
                  >
                    {n.content}
                  </Typography.Paragraph>
                }
              />
            </List.Item>
          )}
        />
      )}
      <Button type="link" block onClick={() => navigate('/notifications')}>
        查看全部
      </Button>
    </div>
  );

  return (
    <Header
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 10,
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        borderBottom: `1px solid ${token.colorBorderSecondary}`,
        backdropFilter: 'saturate(180%) blur(6px)',
      }}
    >
      <Button
        type="text"
        icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
        onClick={toggleCollapsed}
      />
      <Breadcrumb
        items={[{ title: '首页' }, { title: current?.label ?? '' }]}
        style={{ marginInlineStart: 4 }}
      />
      <div style={{ flex: 1 }} />
      <Space size={4}>
        <Popover content={colorPanel} trigger="click" placement="bottomRight">
          <Tooltip title="主题色">
            <Button type="text" icon={<BgColorsOutlined />} />
          </Tooltip>
        </Popover>
        <Tooltip title={mode === 'dark' ? '切到亮色' : '切到暗色'}>
          <Button
            type="text"
            icon={mode === 'dark' ? <SunOutlined /> : <MoonOutlined />}
            onClick={toggleMode}
          />
        </Tooltip>
        <Tooltip title={fullscreen ? '退出全屏' : '全屏'}>
          <Button
            type="text"
            icon={fullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
            onClick={toggleFullscreen}
          />
        </Tooltip>
        <Popover content={notifyPanel} trigger="click" placement="bottomRight" onOpenChange={openNotify}>
          <Badge count={unread} size="small" offset={[-2, 4]}>
            <Button type="text" icon={<BellOutlined />} />
          </Badge>
        </Popover>
        <Dropdown
          menu={{
            items: [
              {
                key: 'logout',
                icon: <LogoutOutlined />,
                label: '退出登录',
                onClick: () => {
                  if (IS_IDP) {
                    // idp 模式:清应用会话键 + 触发 IdP 端 RP-initiated 登出(回到 /login)
                    useAuth.getState().clearSession();
                    void userManager?.signoutRedirect();
                  } else {
                    logout();
                    navigate('/login');
                  }
                },
              },
            ],
          }}
        >
          <span
            style={{
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              paddingInline: 8,
              height: 40,
            }}
          >
            <Avatar size={30} style={{ background: primary }} icon={<UserOutlined />} />
            <span style={{ lineHeight: 1.2 }}>
              <Typography.Text strong style={{ display: 'block', fontSize: 13 }}>
                {user?.realName || user?.username}
              </Typography.Text>
              <span>
                {user?.roles.slice(0, 2).map((r) => (
                  <Tag key={r} color={primary} style={{ marginInlineEnd: 2, lineHeight: '14px' }}>
                    {r}
                  </Tag>
                ))}
              </span>
            </span>
          </span>
        </Dropdown>
      </Space>
    </Header>
  );
}
