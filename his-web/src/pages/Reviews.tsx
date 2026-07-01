import { useEffect, useState } from 'react';
import { Button, Space, App, Tag, Modal, Input } from 'antd';
import { ProTable, type ProColumns } from '@ant-design/pro-components';
import { listPendingReviews, passReview, rejectReview } from '../api/endpoints';
import type { PendingReview } from '../api/types';
import PageContainer from '../components/PageContainer';

export default function Reviews() {
  const { message } = App.useApp();
  const [list, setList] = useState<PendingReview[]>([]);
  const [loading, setLoading] = useState(false);
  const [rejectTarget, setRejectTarget] = useState<PendingReview | null>(null);
  const [opinion, setOpinion] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      setList(await listPendingReviews());
    } catch {
      setList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const onPass = async (encounterId: number) => {
    await passReview(encounterId);
    message.success('审方通过,已放行计费');
    load();
  };

  const onReject = async () => {
    if (!rejectTarget || !opinion.trim()) return;
    await rejectReview(rejectTarget.encounterId, opinion);
    message.success('已驳回');
    setRejectTarget(null);
    setOpinion('');
    load();
  };

  const columns: ProColumns<PendingReview>[] = [
    { title: '就诊号', dataIndex: 'encounterId', width: 90 },
    { title: '患者ID', dataIndex: 'patientId', width: 90 },
    { title: '科室', dataIndex: 'deptCode', width: 100 },
    { title: '医生', dataIndex: 'doctorName', width: 120 },
    {
      title: '药品明细',
      render: (_, r) => (
        <Space direction="vertical" size={2}>
          {r.items.map((it) => (
            <span key={it.orderId}>
              <Tag color="gold">{it.itemName}</Tag>×{it.quantity} = ¥{it.amount}
            </span>
          ))}
        </Space>
      ),
    },
    {
      title: '操作',
      width: 160,
      render: (_, r) => (
        <Space>
          <Button type="primary" size="small" onClick={() => onPass(r.encounterId)}>
            通过
          </Button>
          <Button danger size="small" onClick={() => setRejectTarget(r)}>
            驳回
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer title="药师审方" subtitle="药品医嘱审核，通过后放行计费">
      <ProTable<PendingReview>
        rowKey="encounterId"
        headerTitle="待审处方"
        loading={loading}
        dataSource={list}
        columns={columns}
        search={false}
        pagination={false}
        cardBordered
        options={{ reload: load, density: true, setting: true, fullScreen: true }}
      />

      <Modal
        title={`驳回处方 — 就诊 #${rejectTarget?.encounterId}`}
        open={!!rejectTarget}
        onOk={onReject}
        onCancel={() => setRejectTarget(null)}
        okButtonProps={{ disabled: !opinion.trim() }}
      >
        <Input.TextArea
          rows={3}
          placeholder="请填写驳回意见"
          value={opinion}
          onChange={(e) => setOpinion(e.target.value)}
        />
      </Modal>
    </PageContainer>
  );
}
