import { useState } from 'react';
import {
  Card, Form, Input, Button, DatePicker, InputNumber, Table, Space, App, Tag, Modal, Descriptions, Select,
} from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import { openSchedule, listSchedules, book } from '../api/endpoints';
import type { Schedule, Appointment } from '../api/types';
import { useAuth } from '../store/auth';
import PageContainer from '../components/PageContainer';
import StatusTag from '../components/StatusTag';

export default function Registration() {
  const { message } = App.useApp();
  const isAdmin = useAuth((s) => s.hasAnyRole(['ADMIN']));
  const [openForm] = Form.useForm();
  const [queryDate, setQueryDate] = useState<Dayjs>(dayjs());
  const [queryDept, setQueryDept] = useState('NK'); // 内科
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(false);
  const [bookTarget, setBookTarget] = useState<Schedule | null>(null);
  const [patientId, setPatientId] = useState<number | null>(null);
  const [lastAppt, setLastAppt] = useState<Appointment | null>(null);

  const query = async () => {
    setLoading(true);
    try {
      setSchedules(await listSchedules(queryDate.format('YYYY-MM-DD'), queryDept));
    } catch {
      setSchedules([]);
    } finally {
      setLoading(false);
    }
  };

  const onOpenSchedule = async (v: any) => {
    try {
      await openSchedule({
        doctorId: v.doctorId,
        doctorName: v.doctorName,
        deptCode: v.deptCode,
        scheduleDate: v.scheduleDate.format('YYYY-MM-DD'),
        period: v.period,
        totalSlots: v.totalSlots,
        fee: v.fee,
      });
      message.success('排班成功');
      openForm.resetFields();
      query();
    } catch {
      /* handled */
    }
  };

  const doBook = async () => {
    if (!bookTarget || !patientId) return;
    try {
      const appt = await book(bookTarget.id, patientId);
      setLastAppt(appt);
      message.success(`挂号成功,流水号 ${appt.serialNo}`);
      setBookTarget(null);
      setPatientId(null);
      query();
    } catch {
      /* handled */
    }
  };

  const columns = [
    { title: '排班ID', dataIndex: 'id', width: 80 },
    { title: '科室', dataIndex: 'deptCode' },
    { title: '医生', dataIndex: 'doctorName' },
    { title: '日期', dataIndex: 'scheduleDate' },
    { title: '时段', dataIndex: 'period', render: (v: string) => (v === 'AM' ? '上午' : v === 'PM' ? '下午' : v) },
    {
      title: '号源',
      render: (_: any, r: Schedule) => {
        const full = r.bookedSlots >= r.totalSlots;
        return (
          <Tag color={full ? 'red' : 'green'}>
            {r.bookedSlots}/{r.totalSlots} {full ? '已满' : ''}
          </Tag>
        );
      },
    },
    { title: '挂号费', dataIndex: 'fee', render: (v: number) => `¥${v}` },
    {
      title: '操作',
      render: (_: any, r: Schedule) => (
        <Button type="link" disabled={r.bookedSlots >= r.totalSlots} onClick={() => setBookTarget(r)}>
          挂号
        </Button>
      ),
    },
  ];

  return (
    <PageContainer title="排班挂号" subtitle="号源查询与患者挂号，管理员可开诊排班">
      {isAdmin && (
        <Card title="开诊排班（管理员）" variant="borderless">
          <Form form={openForm} layout="inline" onFinish={onOpenSchedule} style={{ rowGap: 12 }}>
            <Form.Item name="doctorId" rules={[{ required: true }]}>
              <InputNumber placeholder="医生ID" />
            </Form.Item>
            <Form.Item name="doctorName" rules={[{ required: true }]}>
              <Input placeholder="医生姓名" />
            </Form.Item>
            <Form.Item name="deptCode" rules={[{ required: true }]} initialValue="NK">
              <Input placeholder="科室代码" />
            </Form.Item>
            <Form.Item name="scheduleDate" rules={[{ required: true }]} initialValue={dayjs()}>
              <DatePicker />
            </Form.Item>
            <Form.Item name="period" rules={[{ required: true }]} initialValue="AM">
              <Select
                style={{ width: 100 }}
                options={[
                  { value: 'AM', label: '上午' },
                  { value: 'PM', label: '下午' },
                ]}
              />
            </Form.Item>
            <Form.Item name="totalSlots" rules={[{ required: true }]} initialValue={20}>
              <InputNumber placeholder="号源数" min={1} />
            </Form.Item>
            <Form.Item name="fee" rules={[{ required: true }]} initialValue={10}>
              <InputNumber placeholder="挂号费" min={0} prefix="¥" />
            </Form.Item>
            <Button type="primary" htmlType="submit">
              排班
            </Button>
          </Form>
        </Card>
      )}

      <Card title="号源查询 / 挂号" variant="borderless">
        <Space style={{ marginBottom: 16 }} wrap>
          <DatePicker value={queryDate} onChange={(d) => d && setQueryDate(d)} />
          <Input value={queryDept} onChange={(e) => setQueryDept(e.target.value)} placeholder="科室代码" style={{ width: 120 }} />
          <Button type="primary" icon={<SearchOutlined />} onClick={query}>
            查询号源
          </Button>
        </Space>
        <Table rowKey="id" loading={loading} columns={columns} dataSource={schedules} size="middle" pagination={false} />
      </Card>

      {lastAppt && (
        <Card title="最近挂号结果" variant="borderless">
          <Descriptions size="small" column={3} bordered>
            <Descriptions.Item label="挂号ID">{lastAppt.id}</Descriptions.Item>
            <Descriptions.Item label="患者ID">{lastAppt.patientId}</Descriptions.Item>
            <Descriptions.Item label="流水号">
              <Tag color="geekblue">{lastAppt.serialNo}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="科室">{lastAppt.deptCode}</Descriptions.Item>
            <Descriptions.Item label="医生">{lastAppt.doctorName}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <StatusTag status={lastAppt.status} />
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      <Modal
        title={`挂号 — 排班 #${bookTarget?.id} ${bookTarget?.doctorName}`}
        open={!!bookTarget}
        onOk={doBook}
        onCancel={() => setBookTarget(null)}
        okButtonProps={{ disabled: !patientId }}
      >
        <Space>
          患者 ID：
          <InputNumber value={patientId ?? undefined} onChange={(v) => setPatientId(v)} placeholder="患者ID" />
        </Space>
      </Modal>
    </PageContainer>
  );
}
