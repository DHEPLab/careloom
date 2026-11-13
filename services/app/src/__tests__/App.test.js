import React from "react";
import { render, waitFor } from "@testing-library/react-native";
import NetInfo from "@react-native-community/netinfo";
import App from "../App";
import store from "../store";
import Http from "../utils/http";
import { syncOfflineData } from "../cache/uploadData";

jest.mock("react-native-gesture-handler", () => ({}));
jest.mock("fundebug-reactnative", () => ({ init: jest.fn() }));
jest.mock("expo-splash-screen", () => ({
  preventAutoHideAsync: jest.fn(),
  hideAsync: jest.fn(),
}));
jest.mock("expo-asset", () => ({
  Asset: {
    fromModule: () => ({ downloadAsync: jest.fn().mockResolvedValue() }),
  },
}));
jest.mock("@react-navigation/native", () => ({
  NavigationContainer: ({ children }) => children,
}));
jest.mock("../config", () => ({}));
jest.mock("../utils", () => ({
  useBoolState: () => {
    const [value, setValue] = require("react").useState(false);
    return [value, () => setValue(true)];
  },
}));
jest.mock("../constants", () => ({ Colors: { theme: {} } }));
jest.mock("../components", () => ({ Message: () => null }));
jest.mock("../components/modal/confirm", () => ({ ConfirmModal: () => null }));
jest.mock("../components/modal/prompt", () => ({ PromptModal: () => null }));
jest.mock("../store", () => {
  const { createStore, combineReducers } = require("redux");
  return {
    __esModule: true,
    default: createStore(
      combineReducers({
        user: require("../reducers/user").default,
        message: require("../reducers/message").default,
      }),
    ),
  };
});
jest.mock("../navigation/Navigator", () => {
  const React = require("react");
  const { Text } = require("react-native");
  const { useSelector } = require("react-redux");
  return function TestNavigator() {
    const token = useSelector((state) => state.user.userToken);
    return React.createElement(
      Text,
      { testID: "route" },
      token ? "authenticated" : "signed-out",
    );
  };
});

jest.mock("../utils/http", () => ({
  session: jest.fn(),
  get: jest.fn(),
  bindAccount: jest.fn(),
}));
jest.mock("../cache/uploadData", () => ({ syncOfflineData: jest.fn() }));

beforeEach(() => {
  jest.clearAllMocks();
  store.dispatch({ type: "RESTORE_TOKEN", token: "default token" });
  NetInfo.fetch.mockResolvedValue({ isInternetReachable: true });
  NetInfo.addEventListener.mockReturnValue(jest.fn());
  Http.session.mockResolvedValue({ token: null, account: null });
  Http.get.mockResolvedValue({ id: 1 });
  Http.bindAccount.mockResolvedValue();
  syncOfflineData.mockResolvedValue({ uploaded: 0 });
});

it("shows sign-in on a fresh online startup without requesting an authenticated profile", async () => {
  const screen = render(<App />);
  await waitFor(() =>
    expect(screen.getByTestId("route").props.children).toBe("signed-out"),
  );
  expect(Http.get).not.toHaveBeenCalled();
});
it("fails closed when startup fails before session restoration", async () => {
  NetInfo.fetch.mockRejectedValueOnce(
    new Error("synthetic connectivity failure"),
  );
  const screen = render(<App />);
  await waitFor(() =>
    expect(screen.getByTestId("route").props.children).toBe("signed-out"),
  );
});
it("restores a previously bound offline account", async () => {
  NetInfo.fetch.mockResolvedValue({ isInternetReachable: false });
  Http.session.mockResolvedValue({
    token: "synthetic-token",
    account: "https://synthetic.invalid|1",
  });
  const screen = render(<App />);
  await waitFor(() =>
    expect(screen.getByTestId("route").props.children).toBe("authenticated"),
  );
  expect(Http.get).not.toHaveBeenCalled();
});
it("restores a validated online session", async () => {
  Http.session.mockResolvedValue({
    token: "synthetic-token",
    account: "https://synthetic.invalid|1",
  });
  const screen = render(<App />);
  await waitFor(() =>
    expect(screen.getByTestId("route").props.children).toBe("authenticated"),
  );
  expect(Http.bindAccount).toHaveBeenCalledWith(1);
  expect(syncOfflineData).toHaveBeenCalled();
});
it("shows sign-in when online profile validation fails", async () => {
  Http.session.mockResolvedValue({
    token: "synthetic-token",
    account: "https://synthetic.invalid|1",
  });
  Http.get.mockRejectedValueOnce(new Error("synthetic validation failure"));
  const screen = render(<App />);
  await waitFor(() =>
    expect(screen.getByTestId("route").props.children).toBe("signed-out"),
  );
});
