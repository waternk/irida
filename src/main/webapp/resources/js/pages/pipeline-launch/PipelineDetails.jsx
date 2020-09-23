import React, { useEffect, useRef } from "react";
import { Checkbox, Form, Input } from "antd";
import { useLaunchState } from "./launch-context";

/**
 * React component for the editing of launching a pipeline base details, including
 * name and description.
 * @returns {JSX.Element}
 * @constructor
 */
export function PipelineDetails({}) {
  const { name, description, updateDetailsField } = useLaunchState();
  const nameRef = useRef();

  useEffect(() => {
    // Set focus on the name input at mount time.
    setTimeout(() => nameRef.current.focus(), 100);
  }, []);

  // Select all the text in the name input
  // Used so that when focus is set it all gets selected.
  const selectNameText = () => nameRef.current.select();

  return (
    <>
      <Form.Item
        label={i18n("PipelineDetails.name")}
        rules={[
          {
            required: true,
            message: i18n("PipelineDetails.name.required"),
          },
        ]}
      >
        <Input
          type={"text"}
          value={name}
          ref={nameRef}
          onFocus={selectNameText}
          onChange={(e) =>
            updateDetailsField({ field: "name", value: e.target.value })
          }
        />
      </Form.Item>
      <Form.Item label={i18n("PipelineDetails.description")}>
        <Input.TextArea
          value={description}
          onChange={(e) =>
            updateDetailsField({
              field: "description",
              value: e.currentTarget.value,
            })
          }
        />
      </Form.Item>
      <Form.Item>
        <Checkbox onChange={(e) => console.log(e.target.checked)}>
          {i18n("PipelineDetails.shareWithProjects")}
        </Checkbox>
      </Form.Item>
    </>
  );
}
