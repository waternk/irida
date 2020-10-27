import React from "react";
import isEqual from "lodash/isEqual";
import { Form, Input, Modal } from "antd";
import { useLaunchDispatch, useLaunchState } from "../../launch-context";
import { SPACE_SM } from "../../../../styles/spacing";
import { ParametersFooter } from "./ParametersFooter";

/**
 * React component to render a modal window for modifying and saving pipeline
 * parameters.
 *
 * @param {boolean} visible - determines if the modal should be displayed or not.
 * @param {function} closeModal - function to close the modal window.
 * @returns {JSX.Element}
 * @constructor
 */
export function ParametersModal({ visible, closeModal }) {
  /*
   The currently selected parameter set.  If it is the pipelines default set,
   it can be used modified (but not saved over), or it can be saved as a new
   parameter set.
   */
  const { parameterSet } = useLaunchState();

  const {
    dispatchUseModifiedParameters,
    dispatchOverwriteParameterSave,
  } = useLaunchDispatch();

  /*
  Store a copy of the original values to compare against to see if they
  have been modified.
   */
  const originalValues = parameterSet.parameters.reduce(
    (a, b) => ({ ...a, [b.name]: b.value }),
    {}
  );

  /*
  Have any of the field been modified?
   */
  const [modified, setModified] = React.useState(false);

  /*
  Use by the ant form to set original values for the parameters.
   */
  const [fields, setFields] = React.useState([]);

  const [form] = Form.useForm();

  /*
  Set the original value for all fields on the form
   */
  React.useEffect(() => {
    setFields(
      parameterSet.parameters.map((parameter) => ({
        name: [parameter.name],
        value: parameter.value,
      }))
    );
  }, [parameterSet]);

  const saveParameters = (name) => {
    form.validateFields().then((values) => console.log(values));
  };

  const useModifiedParameters = () => {
    form.validateFields().then((values) => {
      if (form.isFieldsTouched()) {
        dispatchUseModifiedParameters(values);
      }
      closeModal();
    });
  };

  const saveModifiedParameters = () => {
    form.validateFields().then((values) => {
      // dispatchUseModifiedParameters(values);
    });
  };

  /**
   * Overwrite an existing saved parameter set with new values.
   */
  const overwriteParameterSet = () => {
    form.validateFields().then(dispatchOverwriteParameterSave);
  };

  /**
   * Helper function to determine if the original parameters of this set have
   * been modified (which would result in updates to the UI).
   *
   * @param {object} changedValues - most recent changed value
   * @param {object} allValues - all current values in the form.
   */
  const onValuesChange = (changedValues, allValues) =>
    setModified(!isEqual(originalValues, allValues));

  return (
    <Modal
      bodyStyle={{
        padding: 0,
      }}
      visible={visible}
      keyboard={false}
      maskClosable={false}
      onCancel={closeModal}
      title={`${parameterSet.label}${modified ? "*" : ""}`}
      width={600}
      footer={
        <ParametersFooter
          modified={modified}
          onCancel={closeModal}
          useModifiedParameters={useModifiedParameters}
        />
      }
    >
      <section style={{ maxHeight: 600, overflow: "auto", padding: SPACE_SM }}>
        <Form
          form={form}
          layout="vertical"
          fields={fields}
          onValuesChange={onValuesChange}
        >
          {parameterSet.parameters.map((parameter) => (
            <Form.Item
              key={parameter.name}
              label={parameter.label}
              name={parameter.name}
            >
              <Input />
            </Form.Item>
          ))}
        </Form>
      </section>
    </Modal>
  );
}