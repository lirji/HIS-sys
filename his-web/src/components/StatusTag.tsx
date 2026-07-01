import { Tag } from 'antd';

// 跨域状态 -> 颜色 + 中文。覆盖医嘱/挂号/账单/退费/就诊常见状态,
// 未知值回退为原文 + 蓝色,避免漏标。
const COLOR: Record<string, string> = {
  CREATED: 'default',
  PENDING_REVIEW: 'gold',
  REJECTED: 'red',
  SUBMITTED: 'blue',
  EXECUTED: 'green',
  CANCELLED: 'default',
  BOOKED: 'blue',
  UNPAID: 'gold',
  PAID: 'green',
  REFUNDED: 'purple',
  REQUESTED: 'gold',
  APPROVED: 'green',
  OPEN: 'processing',
  DIAGNOSED: 'cyan',
  CLOSED: 'default',
};

const LABEL: Record<string, string> = {
  CREATED: '已创建',
  PENDING_REVIEW: '待审方',
  REJECTED: '已驳回',
  SUBMITTED: '已提交',
  EXECUTED: '已执行',
  CANCELLED: '已取消',
  BOOKED: '已挂号',
  UNPAID: '待缴费',
  PAID: '已缴费',
  REFUNDED: '已退费',
  REQUESTED: '待审批',
  APPROVED: '已通过',
  OPEN: '就诊中',
  DIAGNOSED: '已诊断',
  CLOSED: '已结诊',
};

export default function StatusTag({ status }: { status?: string | null }) {
  if (!status) return <Tag>—</Tag>;
  return <Tag color={COLOR[status] ?? 'blue'}>{LABEL[status] ?? status}</Tag>;
}
