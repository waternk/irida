import React, { useEffect, useState } from "react";
import { Avatar, Button, List, Space, Tag, Typography } from "antd";
import { IconDownloadFile, IconMetadataTemplate } from "../../icons/Icons";
import { setBaseUrl } from "../../../utilities/url-utilities";
import { getProjectMetadataTemplates } from "../../../apis/metadata/metadata-templates";

const { Title } = Typography;

export function ProjectMetadataTemplates() {
  const [templates, setTemplates] = useState([]);
  const BASE_URL = setBaseUrl(
    `/projects/${window.project.id}/settings/metadata-templates`
  );

  useEffect(() => {
    getProjectMetadataTemplates(window.project.id).then((data) =>
      setTemplates(data)
    );
  }, []);

  return (
    <Space
      direction="vertical"
      style={{ width: "100%", border: `1px solid blue` }}
    >
      <Title level={2}>{i18n("ProjectMetadataTemplates.title")}</Title>
      <Space>
        <Button href={`${BASE_URL}/new`} className="t-create-template-btn">
          Create New Template
        </Button>
      </Space>
      <List
        bordered
        itemLayout="horizontal"
        dataSource={templates}
        renderItem={(item) => (
          <List.Item
            className="t-template"
            actions={[
              <Button
                shape="circle"
                icon={<IconDownloadFile />}
                href={`${BASE_URL}/${item.id}/excel`}
                key="list-download"
              />,
            ]}
          >
            <List.Item.Meta
              avatar={<Avatar icon={<IconMetadataTemplate />} />}
              title={
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                  }}
                >
                  <a
                    href={setBaseUrl(
                      `/projects/${window.project.id}/metadata-templates/${item.id}`
                    )}
                  >
                    {item.label}
                  </a>
                  <Tag>
                    {i18n("ProjectMetadataTemplates.fields", item.numFields)}
                  </Tag>
                </div>
              }
              description={item.description}
            />
          </List.Item>
        )}
      />
    </Space>
  );
}
