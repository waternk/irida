import React from "react";
import { Divider, Form, Input } from "antd";

/**
 * React component for editing the basic information for launching an IRIDA Workflow Pipeline.
 * @returns {JSX.Element}
 * @constructor
 */
export function LaunchDetails() {
  return (
    <section>
      <Form.Item
        label={i18n("LaunchDetails.name")}
        name="name"
        rules={[
          {
            required: true,
            message: i18n("LaunchDetails.name.required"),
          },
        ]}
      >
        <Input />
      </Form.Item>
      <Form.Item label={i18n("LaunchDetails.description")} name="description">
        <Input.TextArea />
      </Form.Item>
      <Divider />
    </section>
  );
}