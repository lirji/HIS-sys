import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import MainLayout from './layout/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import OidcCallback from './pages/OidcCallback';

// 业务页懒加载:把 echarts(看板) / pro-components(审方·退费) 拆到独立 chunk,
// 首屏只加载框架 + 登录,切换到对应页再按需拉取。
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Patients = lazy(() => import('./pages/Patients'));
const Registration = lazy(() => import('./pages/Registration'));
const Outpatient = lazy(() => import('./pages/Outpatient'));
const Reviews = lazy(() => import('./pages/Reviews'));
const Billing = lazy(() => import('./pages/Billing'));
const Refunds = lazy(() => import('./pages/Refunds'));
const Emr = lazy(() => import('./pages/Emr'));
const Notifications = lazy(() => import('./pages/Notifications'));

const Loading = () => (
  <div style={{ display: 'flex', justifyContent: 'center', padding: '80px 0' }}>
    <Spin size="large" />
  </div>
);

export default function App() {
  return (
    <Suspense fallback={<Loading />}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/oidc-callback" element={<OidcCallback />} />
        <Route
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<Dashboard />} />
          <Route
            path="/patients"
            element={
              <ProtectedRoute roles={['ADMIN', 'NURSE']}>
                <Patients />
              </ProtectedRoute>
            }
          />
          <Route
            path="/registration"
            element={
              <ProtectedRoute roles={['ADMIN', 'NURSE', 'CASHIER']}>
                <Registration />
              </ProtectedRoute>
            }
          />
          <Route
            path="/outpatient"
            element={
              <ProtectedRoute roles={['DOCTOR']}>
                <Outpatient />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reviews"
            element={
              <ProtectedRoute roles={['PHARMACIST']}>
                <Reviews />
              </ProtectedRoute>
            }
          />
          <Route
            path="/billing"
            element={
              <ProtectedRoute roles={['CASHIER', 'ADMIN']}>
                <Billing />
              </ProtectedRoute>
            }
          />
          <Route
            path="/refunds"
            element={
              <ProtectedRoute roles={['ADMIN']}>
                <Refunds />
              </ProtectedRoute>
            }
          />
          <Route
            path="/emr"
            element={
              <ProtectedRoute roles={['DOCTOR', 'ADMIN']}>
                <Emr />
              </ProtectedRoute>
            }
          />
          <Route path="/notifications" element={<Notifications />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
