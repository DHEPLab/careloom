import "react-native-gesture-handler";
import React, { useEffect } from "react";
import NetInfo from "@react-native-community/netinfo";
import { StatusBar, ToastAndroid } from "react-native";
import * as SplashScreen from "expo-splash-screen";
import { NavigationContainer } from "@react-navigation/native";
import { Asset } from "expo-asset";
import fundebug from "fundebug-reactnative";

import "./i18n";

import { Provider, useDispatch, useSelector } from "react-redux";
import {
  closeGlobalSubmitErrorMessage,
  netInfo,
  restoreToken,
} from "./actions";
import { syncOfflineData } from "./cache/uploadData";

import Http from "./utils/http";
import Navigator from "./navigation/Navigator";
import { Colors } from "./constants";
import { useBoolState } from "./utils";
import store from "./store";
import "./config";

import { Message } from "./components";
import { ConfirmModal } from "./components/modal/confirm";
import { PromptModal } from "./components/modal/prompt";

import { useTranslation } from "react-i18next";

export default function App(props) {
  const [isLoadingComplete, loadingComplete] = useBoolState();

  const { t } = useTranslation();
  fundebug.init({
    apikey: "my-secret-token-to-change-in-production",
  });

  // Load any resources or data that we need prior to rendering the app
  useEffect(() => {
    async function loadResourcesAndDataAsync() {
      // Start signed out; only restore a validated or owner-bound offline session.
      store.dispatch(restoreToken(null));
      try {
        SplashScreen.preventAutoHideAsync();

        const net = await NetInfo.fetch();

        // Restore token from async storage
        const { token, account } = await Http.session();

        if (!net.isInternetReachable) {
          store.dispatch(restoreToken(account ? token : null));
          return ToastAndroid.show(t("App:offlineMessage"), ToastAndroid.LONG);
        }

        // pre download offline assets
        Asset.fromModule(
          require("./assets/images/error-message.png"),
        ).downloadAsync();
        try {
          // Check whether the token is valid
          if (token) {
            const profile = await Http.get("/api/account/profile");
            await Http.bindAccount(profile.id);
            store.dispatch(restoreToken(token));
            const sync = await syncOfflineData();
            if (sync.failed || sync.held) {
              ToastAndroid.show(t("App:offlineSyncPending"), ToastAndroid.LONG);
            }
          }
        } catch (e) {
          // Token is invalid
          store.dispatch(restoreToken(null));
          throw e;
        }
      } catch (e) {
        if (e.code === "OFFLINE_ACCOUNT_MISMATCH") {
          ToastAndroid.show(
            t("SignIn:offlineAccountRecovery"),
            ToastAndroid.LONG,
          );
        }
        if (e.code === "OFFLINE_OWNER_UNKNOWN") {
          ToastAndroid.show(t("SignIn:offlineOwnerUnknown"), ToastAndroid.LONG);
        }
        // We might want to provide this error information to an error reporting service
        console.warn("CareLoom startup could not complete");
      } finally {
        loadingComplete();
        SplashScreen.hideAsync();
      }
    }

    loadResourcesAndDataAsync();
    return NetInfo.addEventListener((state) => store.dispatch(netInfo(state)));
  }, []);

  if (!isLoadingComplete && !props.skipLoadingScreen) {
    return null;
  }

  return (
    <Provider store={store}>
      <NavigationContainer theme={Colors.theme}>
        <StatusBar backgroundColor="transparent" translucent={true} />
        <Navigator />
        <GlobalErrorMessage />
        <ConfirmModal />
        <PromptModal />
      </NavigationContainer>
    </Provider>
  );
}

function GlobalErrorMessage() {
  const { visible } = useSelector((state) => state.message);
  const dispatch = useDispatch();
  const { t } = useTranslation("App");

  return (
    <Message
      error
      visible={visible}
      title={t("submitFailed")}
      content={t("networkError")}
      buttonText={t("understood")}
      onButtonPress={() => dispatch(closeGlobalSubmitErrorMessage())}
    />
  );
}
