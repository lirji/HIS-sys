import { useEffect, useState } from 'react';
import { List, Card, Button, Space, App, Tag, Switch, Badge, Empty, Typography } from 'antd';
import { ReloadOutlined, CheckOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { inbox, markRead, markAllRead } from '../api/endpoints';
import type { Notification } from '../api/types';
import PageContainer from '../components/PageContainer';

export default function Notifications() {
  const { message } = App.useApp();
  const [list, setList] = useState<Notification[]>([]);
  const [onlyUnread, setOnlyUnread] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = async (unreadOnly = onlyUnread) => {
    setLoading(true);
    try {
      setList(await inbox(unreadOnly));
    } catch {
      setList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onRead = async (n: Notification) => {
    if (n.read) return;
    await markRead(n.id);
    load();
  };

  const onReadAll = async () => {
    const { updated } = await markAllRead();
    message.success(`已标记 ${updated} 条为已读`);
    load();
  };

  return (
    <PageContainer
      title="通知 / 待办收件箱"
      subtitle="审方、退费、缴费等业务事件的实时通知"
      extra={
        <Space>
          <span style={{ fontSize: 13 }}>只看未读</span>
          <Switch
            checked={onlyUnread}
            onChange={(v) => {
              setOnlyUnread(v);
              load(v);
            }}
          />
          <Button icon={<ReloadOutlined />} onClick={() => load()}>
            刷新
          </Button>
          <Button type="primary" icon={<CheckOutlined />} onClick={onReadAll}>
            全部已读
          </Button>
        </Space>
      }
    >
      <Card variant="borderless">
        {list.length === 0 && !loading ? (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无通知" />
        ) : (
          <List
            loading={loading}
            dataSource={list}
            renderItem={(n) => (
              <List.Item
                actions={[
                  n.read ? (
                    <Tag key="r">已读</Tag>
                  ) : (
                    <Button key="r" type="link" size="small" onClick={() => onRead(n)}>
                      标为已读
                    </Button>
                  ),
                ]}
                style={{ borderRadius: 8, paddingInline: 8 }}
              >
                <List.Item.Meta
                  avatar={<Badge dot={!n.read} offset={[-2, 4]}><Tag color="purple" style={{ margin: 0 }}>{n.type}</Tag></Badge>}
                  title={
                    <span style={{ fontWeight: n.read ? 400 : 600 }}>{n.title}</span>
                  }
                  description={
                    <div>
                      <div>{n.content}</div>
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {n.bizType} #{n.bizId} · {dayjs(n.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                      </Typography.Text>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>
    </PageContainer>
  );
}
