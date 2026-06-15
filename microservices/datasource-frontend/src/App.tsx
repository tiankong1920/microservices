import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from 'antd'
import MainLayout from './layouts/MainLayout'
import Dashboard from './pages/Dashboard'
import DatasourceList from './pages/DatasourceList'
import DatasourceForm from './pages/DatasourceForm'
import TemplateList from './pages/TemplateList'
import AlertConfig from './pages/AlertConfig'
import AuditLog from './pages/AuditLog'

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="datasources" element={<DatasourceList />} />
          <Route path="datasources/create" element={<DatasourceForm />} />
          <Route path="datasources/:id/edit" element={<DatasourceForm />} />
          <Route path="templates" element={<TemplateList />} />
          <Route path="alerts" element={<AlertConfig />} />
          <Route path="audit-logs" element={<AuditLog />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
