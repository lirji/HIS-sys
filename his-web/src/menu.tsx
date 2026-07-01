import {
  DashboardOutlined,
  TeamOutlined,
  ScheduleOutlined,
  MedicineBoxOutlined,
  SafetyCertificateOutlined,
  PayCircleOutlined,
  RollbackOutlined,
  FileTextOutlined,
  BellOutlined,
} from '@ant-design/icons';

// 单一菜单/路由配置源:导航、路由守卫、面包屑都从这里取。
// roles 为空数组表示登录即可访问。
export interface MenuMeta {
  path: string;
  label: string;
  icon: React.ReactNode;
  roles: string[];
}

export const MENUS: MenuMeta[] = [
  { path: '/', label: '工作台', icon: <DashboardOutlined />, roles: [] },
  { path: '/patients', label: '患者主索引', icon: <TeamOutlined />, roles: ['ADMIN', 'NURSE'] },
  { path: '/registration', label: '排班挂号', icon: <ScheduleOutlined />, roles: ['ADMIN', 'NURSE', 'CASHIER'] },
  { path: '/outpatient', label: '门诊医生站', icon: <MedicineBoxOutlined />, roles: ['DOCTOR'] },
  { path: '/reviews', label: '药师审方', icon: <SafetyCertificateOutlined />, roles: ['PHARMACIST'] },
  { path: '/billing', label: '门诊收费', icon: <PayCircleOutlined />, roles: ['CASHIER', 'ADMIN'] },
  { path: '/refunds', label: '退费审批', icon: <RollbackOutlined />, roles: ['ADMIN'] },
  { path: '/emr', label: '病历 / FHIR', icon: <FileTextOutlined />, roles: ['DOCTOR', 'ADMIN'] },
  { path: '/notifications', label: '通知中心', icon: <BellOutlined />, roles: [] },
];

// 路径 -> 菜单元数据,供面包屑/页签标题快速查。
export const MENU_MAP: Record<string, MenuMeta> = Object.fromEntries(
  MENUS.map((m) => [m.path, m]),
);
