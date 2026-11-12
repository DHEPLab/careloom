import React, { useRef, useState } from "react";
import { useDispatch } from "react-redux";
import { Formik } from "formik";
import { useTranslation } from "react-i18next";

import Http from "../utils/http";
import { signIn } from "@/actions";
import { useBoolState } from "@/utils";
import { Layout } from "../constants";
import { styled } from "@/utils/styled";
import {
  Button,
  FormItem,
  Message,
  PasswordInput,
  SpecialInput,
} from "../components";

export default function SignIn() {
  const { t } = useTranslation("SignIn");
  const dispatch = useDispatch();
  const [visible, openSuccessMessage] = useBoolState();
  const [badCredentials, onBadCredentials, resetBadCredentials] =
    useBoolState();
  const [accountError, setAccountError] = useState(false);
  const signingIn = useRef(false);

  function onSubmit(values) {
    if (signingIn.current) return;
    signingIn.current = true;
    resetBadCredentials();
    setAccountError(false);
    Http.post("/api/authenticate", values)
      .then(async (data) => {
        await Http.auth(data.idToken);
        const profile = await Http.get("/api/account/profile");
        await Http.bindAccount(profile.id);
        openSuccessMessage();
        dispatch(signIn(data));
      })
      .catch(async (error) => {
        await Http.signOut();
        if (error.code === "OFFLINE_ACCOUNT_MISMATCH") {
          setAccountError("offlineAccountRecovery");
          return;
        }
        if (error.code === "OFFLINE_OWNER_UNKNOWN") {
          setAccountError("offlineOwnerUnknown");
          return;
        }
        if (error.status === 401) {
          onBadCredentials();
          return;
        }
      })
      .finally(() => {
        signingIn.current = false;
      });
  }

  return (
    <Container>
      <Logo
        resizeMode="contain"
        source={require("../assets/images/logo.png")}
      />
      <Message visible={visible} title={t("loginSuccess")} />
      <Formik initialValues={{}} onSubmit={onSubmit}>
        {({ handleSubmit, values }) => (
          <FormContainer>
            <FormItem name="username" center noBorder>
              <SpecialInput placeholder={t("enterUsername")} />
            </FormItem>
            <FormItem name="password" center noBorder>
              <PasswordInput
                type="special"
                placeholder={t("enterPassword")}
                onEndEditing={handleSubmit}
              />
            </FormItem>
            <ForgetPassword>
              {/* <Button type="link" title={t('forgotPassword')} /> */}
            </ForgetPassword>
            {badCredentials && (
              <BadCredentials>{t("badCredentials")}</BadCredentials>
            )}
            {accountError && <BadCredentials>{t(accountError)}</BadCredentials>}
            <Button
              disabled={!values.username || !values.password}
              size="large"
              title={t("login")}
              onPress={handleSubmit}
            />
          </FormContainer>
        )}
      </Formik>
      <Inset
        resizeMode="contain"
        source={require("../assets/images/login-inset.png")}
      />
    </Container>
  );
}

const Logo = styled.Image`
  width: 136px;
  align-self: center;
`;

const FormContainer = styled.View`
  width: 260px;
  margin: 0 auto;
`;

const BadCredentials = styled.Text`
  color: #ff2e2e;
  font-size: 10px;
  font-weight: bold;
  margin-bottom: 16px;
  margin-top: -10px;
`;

const Inset = styled.Image`
  position: absolute;
  height: 280px;
  width: 400px;
  left: 0;
  bottom: -5px;
  z-index: -1;
`;

const Container = styled.View`
  position: relative;
  background: #fff;
  width: 100%;
  height: ${Layout.window.height}px;
  padding-top: 100px;
`;

const ForgetPassword = styled.View`
  width: 260px;
  margin: 0px auto 10px auto;
  align-items: flex-end;
`;
