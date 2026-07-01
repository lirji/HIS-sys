import { useEffect, useState } from 'react';
import { Button, Space, App, Modal, Input } from 'antd';
import { ProTable, type ProColumns } from '@ant-design/pro-components';
import { listPendingRefunds, approveRefund, rejectRefund } from '../api/endpoints';
import type { RefundRequest } from '../api/types';
import PageContainer from '../components/PageContainer';
import StatusTag from '../components/StatusTag';

export default function Refunds() {
  const { message } = App.useApp();
  const [list, setList] = useState<RefundRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [rejectTarget, setRejectTarget] = useState<RefundRequest | null>(null);
  const [opinion, setOpinion] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      setList(await listPendingRefunds());
    } catch {
      setList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const onApprove = async (id: number) => {
    await approveRefund(id);
    message.success('已批准,账单退费并撤销已执行医嘱');
    load();
  };

  const onReject = async () => {
    if (!rejectTarget || !opinion.trim()) return;
    await rejectRefund(rejectTarget.id, opinion);
    message.success('已驳回退费申请');
    setRejectTarget(null);
    setOpinion('');
    load();
  };

  const columns: ProColumns<RefundRequest>[] = [
    { title: '申请ID', dataIndex: 'id', width: 80 },
    { title: '账单ID', dataIndex: 'invoiceId', width: 80 },
    { title: '就诊号', dataIndex: 'encounterId', width: 90 },
    { title: '金额', dataIndex: 'amount', width: 100, render: (_, r) => `¥${r.amount}` },
    { title: '原因', dataIndex: 'reason', ellipsis: true },
    { title: '申请人', dataIndex: 'applicantName', width: 110 },
    { title: '状态', dataIndex: 'status', width: 100, render: (_, r) => <StatusTag status={r.status} /> },
    {
      title: '操作',
      width: 150,
      render: (_, r) => (
        <Space>
          <Button type="primary" size="small" onClick={() => onApprove(r.id)}>
            批准
          </Button>
          <Button danger size="small" onClick={() => setRejectTarget(r)}>
            驳回
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer title="退费审批" subtitle="管理员审批退费申请，批准后自动撤销已执行医嘱">
      <ProTable<RefundRequest>
        rowKey="id"
        headerTitle="待审退费申请"
        loading={loading}
        dataSource={list}
        columns={columns}
        search={false}
        pagination={false}
        cardBordered
        options={{ reload: load, density: true, setting: true, fullScreen: true }}
      />

      <Modal
        title={`驳回退费 — 申请 #${rejectTarget?.id}`}
        open={!!rejectTarget}
        onOk={onReject}
        onCancel={() => setRejectTarget(null)}
        okButtonProps={{ disabled: !opinion.trim() }}
      >
        <Input.TextArea rows={3} placeholder="驳回意见" value={opinion} onChange={(e) => setOpinion(e.target.value)} />
      </Modal>
    </PageContainer>
  );
}
