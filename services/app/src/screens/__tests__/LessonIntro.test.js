import React from "react";
import { render, fireEvent, waitFor, act } from "@testing-library/react-native";
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
    require("react").createElement(require("react-native").Pressable, {
      testID: "next",
      onPress,
    }),
}));
const account = "https://synthetic.invalid|1";
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
  const screen = render(<LessonIntro navigation={navigation} route={route} />);
  await waitFor(() => expect(release).toBeDefined());
  fireEvent.press(screen.getByTestId("next"));
  expect(Location.requestForegroundPermissionsAsync).not.toHaveBeenCalled();
  await act(async () => {
    release();
  });
  await waitFor(() =>
    expect(navigation.navigate).toHaveBeenCalledWith(
      "LessonModules",
      route.params,
    ),
  );
  expect(storage.setNextModule).toHaveBeenCalledWith(3, 7, account);
});
it("keeps the user on the intro if visit initialization fails", async () => {
  storage.setUncommittedVisitStatus.mockRejectedValueOnce(
    new Error("synthetic save failure"),
  );
  const navigation = { navigate: jest.fn(), goBack: jest.fn() };
  const screen = render(
    <LessonIntro
      navigation={navigation}
      route={{ params: { id: 1, visitId: 7 } }}
    />,
  );
  await act(async () => {
    fireEvent.press(screen.getByTestId("next"));
  });
  expect(navigation.navigate).not.toHaveBeenCalled();
  expect(Location.requestForegroundPermissionsAsync).not.toHaveBeenCalled();
});

it("does not write visit status or progress while previewing curriculum", () => {
  render(
    <LessonIntro
      navigation={{ navigate: jest.fn(), goBack: jest.fn() }}
      route={{ params: { id: 1, preview: true } }}
    />,
  );
  expect(storage.setUncommittedVisitStatus).not.toHaveBeenCalled();
  expect(storage.setNextModule).not.toHaveBeenCalled();
});
