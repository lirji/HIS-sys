import { Navigate, useLocation } from 'react-router-dom';
import { Result, Button } from 'antd';
import { useAuth } from '../store/auth';

interface Props {
  roles?: string[];
  children: React.ReactNode;
}

export default function ProtectedRoute({ roles = [], children }: Props) {
  const location = useLocation();
  const { token, hasAnyRole } = useAuth();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (!hasAnyRole(roles)) {
    return (
      <Result
        status="403"
        title="403"
        subTitle="当前账号无权访问该模块"
        extra={
          <Button type="primary" href="/">
            返回首页
          </Button>
        }
      />
    );
  }
  return <>{children}</>;
}
