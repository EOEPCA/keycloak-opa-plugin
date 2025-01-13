import { useTranslation } from "react-i18next";
import { Controller, useFormContext } from "react-hook-form";
import { Checkbox, FormGroup } from "@patternfly/react-core";

import { HelpItem } from "ui-shared";
import { KeycloakTextInput } from "../../../components/keycloak-text-input/KeycloakTextInput";

export const Opa = () => {
  const { t } = useTranslation();
  const {
    control,
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <>
      <FormGroup
        label="OPA Policy Path"
        fieldId="policyPath"
        helperTextInvalid={t("required")}
        validated={errors.policyPath ? "error" : "default"}
        isRequired
        labelIcon={
          <HelpItem
            helpText={t("policyPathHelp")}
            fieldLabelId="policyPath"
          />
        }
      >
        <KeycloakTextInput
          id="policyPath"
          data-testid="policyPath"
          validated={errors.policyPath ? "error" : "default"}
          {...register("policyPath", { required: true })}
        />
      </FormGroup>
      <FormGroup
        label="Include Permissions in Input"
        fieldId="includePermission"
        labelIcon={
          <HelpItem
            helpText={t("includePermissionHelp")}
            fieldLabelId="includePermission"
          />
        }
      >
      <Controller
        name="includePermission"
        defaultValue={true}
        control={control}
        render={({ field }) => (
          <Checkbox
            id="includePermission"
            isChecked={field.value}
            onChange={field.onChange}
          />
        )}
      />
      </FormGroup>
      <FormGroup
        label="Include Resource in Input"
        fieldId="includeResource"
        labelIcon={
          <HelpItem
            helpText={t("includeResourceHelp")}
            fieldLabelId="includeResource"
          />
        }
      >
      <Controller
        name="includeResource"
        defaultValue={true}
        control={control}
        render={({ field }) => (
          <Checkbox
            id="includeResource"
            isChecked={field.value}
            onChange={field.onChange}
          />
        )}
      />
      </FormGroup>

    </>
  );
};
