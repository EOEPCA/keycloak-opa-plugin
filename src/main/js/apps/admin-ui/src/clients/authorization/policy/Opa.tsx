import { useTranslation } from "react-i18next";
import { SwitchControl, TextControl } from "@keycloak/keycloak-ui-shared";

export const Opa = () => {
  const { t } = useTranslation();

  return (
    <>
      <TextControl
        name="policyPath"
        label="OPA Policy Path"
        labelIcon={t("policyPathHelp")}
        rules={{ required: t("required") }}
      />
      <SwitchControl
        name="includePermission"
        label="Include Permissions in Input"
        labelIcon={t("includePermissionHelp")}
        labelOn={t("yes")}
        labelOff={t("no")}
      />
      <SwitchControl
        name="includeResource"
        label="Include Resource in Input"
        labelIcon={t("includeResourceHelp")}
        labelOn={t("yes")}
        labelOff={t("no")}
      />
    </>
  );
};
