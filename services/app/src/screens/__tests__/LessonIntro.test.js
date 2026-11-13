import React from "react";
import TestRenderer, { act } from "react-test-renderer";
import LessonIntro from "../LessonIntro";
import storage from "../../cache/storage";
import Http from "../../utils/http";
import * as Location from "expo-location";

jest.mock("../../cache/storage", () => ({
  useLesson: () => [{ name: "Synthetic lesson" }],
  setUncommittedVisitStatus: jest.fn(),
  getNextModule: jest.fn(),
  setNextModule: jest.fn(),
}));
jest.mock("../../utils/http", () => ({ session: jest.fn() }));
jest.mock("expo-location", () => ({
  requestForegroundPermissionsAsync: jest.fn(),
  hasServicesEnabledAsync: jest.fn(),
}));
jest.mock("../../components", () => ({
  GhostNavigatorHeader: () => null,
  BottomRightBackground: () => null,
  Button: ({ onPress }) =>
    require("react").createElement("Button", {
      testID: "next",
      onPress,
    }),
}));
const account = "https://synthetic.invalid|1";
let screen;
async function renderIntro(props) {
  await act(async () => {
    screen = TestRenderer.create(<LessonIntro {...props} />);
  });
}
function pressNext() {
  return screen.root.findByProps({ testID: "next" }).props.onPress();
}
afterEach(() => {
  act(() => screen?.unmount());
  screen = undefined;
});
beforeEach(() => {
  jest.clearAllMocks();
  Http.session.mockResolvedValue({ account, token: "synthetic-token" });
  storage.setUncommittedVisitStatus.mockResolvedValue();
  storage.getNextModule.mockResolvedValue(2);
  storage.setNextModule.mockResolvedValue();
  Location.requestForegroundPermissionsAsync.mockResolvedValue({
    status: "granted",
  });
  Location.hasServicesEnabledAsync.mockResolvedValue(true);
});
it("waits for visit-specific resume initialization before opening the modules", async () => {
  let release;
  storage.setUncommittedVisitStatus.mockImplementationOnce(
    () =>
      new Promise((resolve) => {
        release = resolve;
      }),
  );
  const navigation = { navigate: jest.fn(), goBack: jest.fn() };
  const route = {
    params: { id: 1, visitId: 7, continue: true, nextModuleIndex: 3 },
  };
  await renderIntro({ navigation, route });
  expect(release).toBeDefined();
  let pressing;
  act(() => {
    pressing = pressNext();
  });
  expect(Location.requestForegroundPermissionsAsync).not.toHaveBeenCalled();
  await act(async () => {
    release();
    await pressing;
  });
  expect(navigation.navigate).toHaveBeenCalledWith(
    "LessonModules",
    route.params,
  );
  expect(storage.setNextModule).toHaveBeenCalledWith(3, 7, account);
});
it("keeps the user on the intro if visit initialization fails", async () => {
  storage.setUncommittedVisitStatus.mockRejectedValueOnce(
    new Error("synthetic save failure"),
  );
  const navigation = { navigate: jest.fn(), goBack: jest.fn() };
  await renderIntro({ navigation, route: { params: { id: 1, visitId: 7 } } });
  await act(async () => {
    await pressNext();
  });
  expect(navigation.navigate).not.toHaveBeenCalled();
  expect(Location.requestForegroundPermissionsAsync).not.toHaveBeenCalled();
});

it("does not write visit status or progress while previewing curriculum", async () => {
  await renderIntro({
    navigation: { navigate: jest.fn(), goBack: jest.fn() },
    route: { params: { id: 1, preview: true } },
  });
  expect(storage.setUncommittedVisitStatus).not.toHaveBeenCalled();
  expect(storage.setNextModule).not.toHaveBeenCalled();
});
