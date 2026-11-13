import { ToastAndroid } from "react-native";
import mockRNCNetInfo from "@react-native-community/netinfo/jest/netinfo-mock.js";
import "./src/i18n";

jest.mock("@react-native-community/netinfo", () => mockRNCNetInfo);

jest.mock("@react-native-async-storage/async-storage", () =>
  require("@react-native-async-storage/async-storage/jest/async-storage-mock"),
);

ToastAndroid.showWithGravity = jest.fn();
ToastAndroid.show = jest.fn();
global.fetch = jest.fn();
