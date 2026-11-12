function response(data, ok = true) {
  return {
    ok,
    status: ok ? 200 : 401,
    headers: { get: () => "application/json" },
    json: async () => data,
  };
}

jest.mock("../../store", () => ({ dispatch: jest.fn() }));
jest.mock("../../constants/Config", () => ({
  apiHost: "https://synthetic.invalid",
}));

let AsyncStorage;
let http;
beforeEach(async () => {
  jest.resetModules();
  process.env.EXPO_PUBLIC_API_URL = "https://synthetic.invalid";
  AsyncStorage = require("@react-native-async-storage/async-storage");
  await AsyncStorage.clear();
  global.fetch = jest.fn().mockResolvedValue(response({ id: 1 }));
});

it("persists token and authenticated account together, then logout retains pending work", async () => {
  http = require("../http").default;
  const storage = require("../../cache/storage").default;
  await http.auth("synthetic-token");
  expect(await AsyncStorage.getItem("CARELOOM_SESSION")).toBeNull();
  await http.bindAccount(1);
  const saved = JSON.parse(await AsyncStorage.getItem("CARELOOM_SESSION"));
  expect(saved).toEqual({
    token: "synthetic-token",
    account: "https://synthetic.invalid|1",
  });
  await storage.addOfflineBaby({ name: "Synthetic family" }, saved.account);
  await http.signOut();
  expect(await http.session()).toEqual({ token: "", account: null });
  expect(await storage.getOfflineBabies()).toHaveLength(1);
  expect(await AsyncStorage.getItem("CARELOOM_SESSION")).toBeNull();
});

it("does not restore credentials from another backend or an unbound legacy token", async () => {
  await AsyncStorage.setItem(
    "CARELOOM_SESSION",
    JSON.stringify({
      token: "synthetic-old-token",
      account: "https://other-synthetic.invalid|1",
    }),
  );
  await AsyncStorage.setItem("JWT_TOKEN", "synthetic-legacy-token");
  http = require("../http").default;
  expect((await http.session()).token).toBeFalsy();
  expect((await http.session()).account).toBeFalsy();
  expect(global.fetch).not.toHaveBeenCalled();
  expect(await AsyncStorage.getItem("CARELOOM_SESSION")).not.toBeNull();
});

it("ignores a late unauthorized response from an older session token", async () => {
  http = require("../http").default;
  await http.auth("synthetic-old-token");
  await http.bindAccount(1);
  let finish;
  global.fetch.mockImplementationOnce(
    () =>
      new Promise((resolve) => {
        finish = resolve;
      }),
  );
  const request = http.post(
    "/api/babies",
    { synthetic: true },
    { token: "synthetic-old-token" },
  );
  await http.auth("synthetic-new-token");
  await http.bindAccount(1);
  finish(response({ status: 401 }, false));
  await expect(request).rejects.toMatchObject({ status: 401 });
  expect((await http.session()).token).toBe("synthetic-new-token");
  expect(JSON.parse(await AsyncStorage.getItem("CARELOOM_SESSION")).token).toBe(
    "synthetic-new-token",
  );
});
