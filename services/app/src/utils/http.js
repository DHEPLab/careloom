import AsyncStorage from "@react-native-async-storage/async-storage";
import { ToastAndroid } from "react-native";

import Config from "../constants/Config";
import store from "../store";
import { openGlobalSubmitErrorMessage, restoreToken } from "@/actions";
import i18next from "i18next";
import offlineStorage from "../cache/storage";

// fetch timeout 15s
const timeout = 15000;
const Host = Config.apiHost;
let Token;
let Account;

const sessionReady = (async () => {
  const saved = await AsyncStorage.getItem("CARELOOM_SESSION");
  if (saved) {
    let session;
    try {
      session = JSON.parse(saved);
    } catch (_) {
      return;
    }
    // Do not forward credentials to a newly configured backend. Keep the saved
    // session and offline queues held for the original deployment.
    if (
      Host &&
      typeof session?.account === "string" &&
      session.account.startsWith(`${Host}|`) &&
      typeof session.token === "string"
    ) {
      Token = session.token;
      Account = session.account;
    }
  } else {
    // Legacy tokens have no recorded backend. Require an explicit new login.
    Token = null;
  }
})();

async function clearAuthentication() {
  Token = "";
  Account = null;
  await AsyncStorage.removeItem("CARELOOM_SESSION");
  await AsyncStorage.removeItem("JWT_TOKEN");
}

export function responseContentTypeJSON(response) {
  return (
    response.headers.get("content-type") === "application/json" ||
    response.headers.get("content-type") === "application/problem+json"
  );
}

async function cleanToken(requestToken) {
  if (requestToken !== Token) return;
  store.dispatch(restoreToken(null));
  await clearAuthentication();
}

async function onResponseError(url, status, data, requestToken) {
  let msg = i18next.t("App:networkError");
  switch (status) {
    case 502:
      msg = i18next.t("App:networkError");
    case 500:
      break;
    case 404:
      return;
    case 401:
      cleanToken(requestToken);
      return;
    default:
      if (data.violations) {
        msg = i18next.t("App:formValidate");
      } else if (data.detail) {
        msg = data.detail;
      }
  }
  console.warn("CareLoom request failed", status);
  ToastAndroid.show(msg, ToastAndroid.LONG);
}

function request(fetchPromise, method, { silence, url, token }) {
  return new Promise((resolve, reject) => {
    fetchPromise
      .then(async (response) => {
        const data = responseContentTypeJSON(response)
          ? await response.json()
          : { text: await response.text() };
        if (response.ok) {
          resolve(data);
        } else {
          !silence && onResponseError(url, response.status, data, token);
          reject(data);
        }
      })
      .catch((error) => {
        if (!silence) {
          if (method === "GET") {
            ToastAndroid.show(
              url + " " + i18next.t("App:networkError"),
              ToastAndroid.SHORT,
            );
          } else {
            store.dispatch(openGlobalSubmitErrorMessage());
          }
        }
        reject(error);
        console.warn("CareLoom request failed", method);
      });
  });
}

function objToQueryString(obj) {
  const keyValuePairs = [];
  for (const key in obj) {
    keyValuePairs.push(
      encodeURIComponent(key) + "=" + encodeURIComponent(obj[key]),
    );
  }
  return keyValuePairs.join("&");
}

export default {
  async auth(token) {
    await sessionReady;
    // Do not persist a new token alongside the previous account's identity.
    Token = token;
    Account = null;
  },
  async bindAccount(profileId) {
    await sessionReady;
    if (!Token || profileId === undefined || profileId === null || !Host) {
      throw new Error("Authenticated account and API endpoint are required.");
    }
    const account = `${Host}|${profileId}`;
    await offlineStorage.activateOfflineAccount(account);
    await AsyncStorage.setItem(
      "CARELOOM_SESSION",
      JSON.stringify({ token: Token, account }),
    );
    Account = account;
    await AsyncStorage.removeItem("JWT_TOKEN");
  },
  async session() {
    await sessionReady;
    return { token: Token, account: Account };
  },
  async token() {
    await sessionReady;
    return Token;
  },
  async signOut() {
    await sessionReady;
    await clearAuthentication();
  },
  post(url, body, options = {}) {
    const requestToken = options.token ?? Token;
    return request(
      fetch(`${Host}${url}`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${requestToken}`,
        },
        body: JSON.stringify(body),
        timeout,
      }),
      "POST",
      { url, token: requestToken },
    );
  },
  put(url, body) {
    return request(
      fetch(`${Host}${url}`, {
        method: "PUT",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${Token}`,
        },
        body: JSON.stringify(body),
        timeout,
      }),
      "PUT",
      { url, token: Token },
    );
  },
  silencePut(url, body) {
    return request(
      fetch(`${Host}${url}`, {
        method: "PUT",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${Token}`,
        },
        body: JSON.stringify(body),
        timeout,
      }),
      "PUT",
      { silence: true, url, token: Token },
    );
  },
  get(url, params) {
    return request(
      fetch(`${Host}${url}?${objToQueryString(params)}`, {
        method: "GET",
        headers: {
          Accept: "application/json",
          Authorization: `Bearer ${Token}`,
        },
        timeout,
      }),
      "GET",
      { url, token: Token },
    );
  },
  silenceGet(url, params) {
    return request(
      fetch(`${Host}${url}?${objToQueryString(params)}`, {
        method: "GET",
        headers: {
          Accept: "application/json",
          Authorization: `Bearer ${Token}`,
        },
        timeout,
      }),
      "GET",
      { silence: true, url, token: Token },
    );
  },
  delete(url) {
    return request(
      fetch(`${Host}${url}`, {
        method: "DELETE",
        headers: {
          Accept: "application/json",
          Authorization: `Bearer ${Token}`,
        },
        timeout,
      }),
      "DELETE",
      { url, token: Token },
    );
  },
};
