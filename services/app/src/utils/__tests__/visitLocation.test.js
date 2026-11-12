import Http from "../http";
import * as Location from "expo-location";
import { uploadVisitLocation } from "../visit";

jest.mock("../http", () => ({ session: jest.fn(), post: jest.fn() }));
jest.mock("expo-location", () => ({
  hasServicesEnabledAsync: jest.fn(),
  getCurrentPositionAsync: jest.fn(),
  Accuracy: { Highest: 6 },
}));
const session = {
  account: "https://synthetic.invalid|1",
  token: "synthetic-token",
};
beforeEach(() => {
  jest.clearAllMocks();
  Http.session.mockResolvedValue(session);
  Http.post.mockResolvedValue({});
  Location.hasServicesEnabledAsync.mockResolvedValue(true);
  Location.getCurrentPositionAsync.mockResolvedValue({
    coords: { latitude: 0, longitude: 0 },
  });
});
it("uses the captured authenticated session for a visit location upload", async () => {
  await uploadVisitLocation(1, 7, session);
  expect(Http.post).toHaveBeenCalledWith(
    "/api/visits/upload/location",
    { babyId: 1, visitId: 7, latitude: 0, longitude: 0 },
    { token: session.token },
  );
});
it("does not upload an old visit location after an account switch while GPS is pending", async () => {
  let resolveLocation;
  Location.getCurrentPositionAsync.mockImplementation(
    () =>
      new Promise((resolve) => {
        resolveLocation = resolve;
      }),
  );
  const pending = uploadVisitLocation(1, 7, session);
  await new Promise((resolve) => setImmediate(resolve));
  Http.session.mockResolvedValue({
    account: "https://synthetic.invalid|2",
    token: "synthetic-other-token",
  });
  resolveLocation({ coords: { latitude: 0, longitude: 0 } });
  await pending;
  expect(Http.post).not.toHaveBeenCalled();
});
