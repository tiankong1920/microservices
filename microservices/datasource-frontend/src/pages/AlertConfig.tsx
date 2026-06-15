import React from 'react'
import { Card, Typography } from 'antd'

const { Title, Paragraph } = Typography

const AlertConfig: React.FC = () => {
  return (
    <div>
      <div className="page-header">
        <div className="page-title">告警配置</div>
        <div className="page-subtitle">配置数据源连接告警规则</div>
      </div>

      <Card>
        <Title level={4}>告警配置管理</Title>
        <Paragraph>
          此页面用于配置告警规则和通知渠道，支持邮件、短信、企业微信、钉钉等多种告警方式。
        </Paragraph>
      </Card>
    </div>
  )
}

export default AlertConfig
