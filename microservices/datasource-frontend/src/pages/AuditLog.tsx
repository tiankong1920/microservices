import React from 'react'
import { Card, Typography } from 'antd'

const { Title, Paragraph } = Typography

const AuditLog: React.FC = () => {
  return (
    <div>
      <div className="page-header">
        <div className="page-title">审计日志</div>
        <div className="page-subtitle">查看系统操作审计记录</div>
      </div>

      <Card>
        <Title level={4}>审计日志查询</Title>
        <Paragraph>
          此页面用于查询和导出系统操作审计日志，包括配置变更、连接状态变化、用户操作等记录。
        </Paragraph>
      </Card>
    </div>
  )
}

export default AuditLog
