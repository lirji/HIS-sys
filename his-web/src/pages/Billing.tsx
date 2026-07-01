import { useState } from 'react';
import {
  Card, InputNumber, Button, Space, App, Descriptions, Table, Select, Modal, Input, Statistic, Empty,
} from 'antd';
import { getInvoiceByEncounter, payInvoice, requestRefund } from '../api/endpoints';
import type { Invoice } from '../api/types';
import { useAuth } from '../store/auth';
import PageContainer from '../components/PageContainer';
import StatusTag from '../components/StatusTag';

export default function Billing() {
  const { message } = App.useApp();
  const isCashier = useAuth((s) => s.hasAnyRole(['CASHIER']));
  const [encounterId, setEncounterId] = useState<number | null>(null);
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [payMethod, setPayMethod] = useState('CASH');
  const [refundOpen, setRefundOpen] = useState(false);
  const [reason, setReason] = useState('');

  const load = async () => {
    if (!encounterId) return;
    try {
      setInvoice(await getInvoiceByEncounter(encounterId));
    } catch {
      setInvoice(null);
    }
  };

  const onPay = async () => {
    if (!invoice) return;
    const updated = await payInvoice(invoice.id, payMethod);
    setInvoice(updated);
    message.success('缴费成功,已推进医嘱执行 / 生成病历');
  };

  const onRequestRefund = async () => {
    if (!invoice || !reason.trim()) return;
    await requestRefund(invoice.id, reason);
    message.success('退费申请已提交,等待管理员审批');
    setRefundOpen(false);
    setReason('');
    await load();
  };

  return (
    <PageContainer title="门诊收费" subtitle="按就诊号查询账单、缴费与发起退费">
      <Card title="账单查询" variant="borderless">
        <Space>
          就诊号：
          <InputNumber value={encounterId ?? undefined} onChange={(v) => setEncounterId(v)} onPressEnter={load} placeholder="encounterId" />
          <Button type="primary" onClick={load}>
            查账单
          </Button>
        </Space>
      </Card>

      {invoice ? (
        <Card
          title={`账单 #${invoice.id}`}
          variant="borderless"
          extra={<StatusTag status={invoice.status} />}
        >
          <Space size="large" style={{ marginBottom: 16 }}>
            <Statistic title="应收金额" value={invoice.totalAmount} prefix="¥" precision={2} valueStyle={{ color: '#cf1322' }} />
            {invoice.payMethod && <Statistic title="支付方式" value={invoice.payMethod} />}
          </Space>
          <Descriptions size="small" column={3} bordered style={{ marginBottom: 16 }}>
            <Descriptions.Item label="患者ID">{invoice.patientId}</Descriptions.Item>
            <Descriptions.Item label="科室">{invoice.deptCode}</Descriptions.Item>
            <Descriptions.Item label="就诊号">{invoice.encounterId}</Descriptions.Item>
          </Descriptions>
          <Table
            rowKey="orderId"
            size="middle"
            pagination={false}
            dataSource={invoice.items}
            columns={[
              { title: '医嘱ID', dataIndex: 'orderId' },
              { title: '项目', dataIndex: 'itemName' },
              { title: '数量', dataIndex: 'quantity' },
              { title: '金额', dataIndex: 'amount', render: (v: number) => `¥${v}` },
            ]}
          />

          <Space style={{ marginTop: 16 }}>
            {invoice.status === 'UNPAID' && (
              <>
                <Select
                  value={payMethod}
                  onChange={setPayMethod}
                  style={{ width: 140 }}
                  options={[
                    { value: 'CASH', label: '现金' },
                    { value: 'WECHAT', label: '微信' },
                    { value: 'ALIPAY', label: '支付宝' },
                    { value: 'CARD', label: '银行卡' },
                  ]}
                />
                <Button type="primary" onClick={onPay}>
                  缴费
                </Button>
              </>
            )}
            {invoice.status === 'PAID' && isCashier && (
              <Button danger onClick={() => setRefundOpen(true)}>
                发起退费
              </Button>
            )}
          </Space>
        </Card>
      ) : (
        <Card variant="borderless">
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="输入就诊号查询账单" />
        </Card>
      )}

      <Modal
        title="发起退费申请"
        open={refundOpen}
        onOk={onRequestRefund}
        onCancel={() => setRefundOpen(false)}
        okButtonProps={{ disabled: !reason.trim() }}
      >
        <Input.TextArea rows={3} placeholder="退费原因" value={reason} onChange={(e) => setReason(e.target.value)} />
      </Modal>
    </PageContainer>
  );
}
