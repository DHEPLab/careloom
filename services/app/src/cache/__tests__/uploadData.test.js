import AsyncStorage from "@react-native-async-storage/async-storage";
import storage from "../storage";
import http from "../../utils/http";
import { syncOfflineData } from "../uploadData";

jest.mock("../../utils/http", () => ({ session: jest.fn(), post: jest.fn() }));

const account = "https://synthetic.invalid|1";
function deferred() {
  let resolve;
  const promise = new Promise((r) => {
    resolve = r;
  });
  return { promise, resolve };
}

beforeEach(async () => {
  await AsyncStorage.clear();
  jest.clearAllMocks();
  http.session.mockResolvedValue({ account, token: "synthetic-token" });
  http.post.mockResolvedValue({ id: 100 });
  await storage.activateOfflineAccount(account);
});

it("keeps failed records and visits until family uploads succeed, then resumes", async () => {
  const first = await storage.addOfflineBaby({ name: "Synthetic A" }, account);
  const failed = await storage.addOfflineBaby({ name: "Synthetic B" }, account);
  await storage.addOfflineVisit({ babyId: 100, lessonId: 2 }, account);
  http.post
    .mockResolvedValueOnce({ id: 100 })
    .mockRejectedValueOnce(new Error("synthetic failure"));
  expect(await syncOfflineData()).toEqual({ uploaded: 1, failed: true });
  expect((await storage.getOfflineBabies()).map((r) => r._offlineId)).toEqual([
    failed._offlineId,
  ]);
  expect(await storage.getOfflineVisits()).toHaveLength(1);
  expect(http.post).toHaveBeenCalledTimes(2);
  expect(http.post.mock.calls[0][1]).not.toHaveProperty("_offlineId");
  expect(first._offlineId).not.toBe(failed._offlineId);
  expect(await syncOfflineData()).toEqual({ uploaded: 2 });
  expect(await storage.pendingOfflineRecords()).toBe(0);
});

it("waits for acknowledgement and preserves concurrently added records", async () => {
  const gate = deferred();
  await storage.addOfflineBaby({ name: "Synthetic A" }, account);
  http.post.mockImplementationOnce(() => gate.promise);
  const firstSync = syncOfflineData();
  expect(syncOfflineData()).toBe(firstSync);
  // Let the upload start without acknowledging it.
  await new Promise((r) => setImmediate(r));
  expect(await storage.getOfflineBabies()).toHaveLength(1);
  await Promise.all([
    storage.addOfflineBaby({ name: "Synthetic B" }, account),
    storage.addOfflineBaby({ name: "Synthetic C" }, account),
  ]);
  expect(http.post).toHaveBeenCalledTimes(1);
  gate.resolve({ id: 100 });
  await firstSync;
  expect((await storage.getOfflineBabies()).map((r) => r.name)).toEqual([
    "Synthetic B",
    "Synthetic C",
  ]);
  await syncOfflineData();
  expect(await storage.pendingOfflineRecords()).toBe(0);
  expect(http.post).toHaveBeenCalledTimes(3);
  await syncOfflineData();
  expect(http.post).toHaveBeenCalledTimes(3);
});

it("holds queued work after sign-out and blocks another account until empty", async () => {
  await storage.addOfflineBaby({ name: "Synthetic A" }, account);
  http.session.mockResolvedValue({ account: null, token: null });
  expect(await syncOfflineData()).toEqual({ uploaded: 0, held: true });
  await expect(
    storage.activateOfflineAccount("https://synthetic.invalid|2"),
  ).rejects.toMatchObject({ code: "OFFLINE_ACCOUNT_MISMATCH" });
  expect(await storage.getOfflineBabies()).toHaveLength(1);
  http.session.mockResolvedValue({ account, token: "synthetic-renewed-token" });
  await storage.activateOfflineAccount(account);
  await syncOfflineData();
  await storage.addLesson({ id: 1, name: "Synthetic private cache" });
  await storage.activateOfflineAccount("https://synthetic.invalid|2");
  expect(await storage.getLesson(1)).toBeNull();
});

it("stops after a session change and keeps unsent records", async () => {
  const gate = deferred();
  await storage.addOfflineBaby({ name: "Synthetic A" }, account);
  await storage.addOfflineBaby({ name: "Synthetic B" }, account);
  http.post.mockImplementationOnce(() => gate.promise);
  const syncing = syncOfflineData();
  await new Promise((r) => setImmediate(r));
  http.session.mockResolvedValue({ account: null, token: null });
  gate.resolve({ id: 100 });
  expect(await syncing).toEqual({ uploaded: 1, held: true });
  expect((await storage.getOfflineBabies()).map((r) => r.name)).toEqual([
    "Synthetic B",
  ]);
  expect(http.post).toHaveBeenCalledWith(
    "/api/babies",
    { name: "Synthetic A" },
    { token: "synthetic-token" },
  );
});

it("preserves legacy unowned records instead of guessing an owner", async () => {
  await AsyncStorage.clear();
  const legacy = JSON.stringify([{ name: "Synthetic legacy" }]);
  await AsyncStorage.setItem("OFFLINEBABIES", legacy);
  await expect(storage.activateOfflineAccount(account)).rejects.toMatchObject({
    code: "OFFLINE_OWNER_UNKNOWN",
  });
  expect(await AsyncStorage.getItem("OFFLINEBABIES")).toBe(legacy);
  expect(await AsyncStorage.getItem("OFFLINE_ACCOUNT")).toBeNull();
});

it("blocks account switching while visit status work remains and acknowledges only one unchanged visit", async () => {
  await storage.setUncommittedVisitStatus(
    1,
    "UNDONE",
    "2026-11-12T10:00:00",
    account,
  );
  await storage.setUncommittedVisitStatus(
    2,
    "DONE",
    "2026-11-12T11:00:00",
    account,
  );
  const snapshot = await storage.getUncommittedVisitStatus();
  await expect(
    storage.activateOfflineAccount("https://synthetic.invalid|2"),
  ).rejects.toMatchObject({ code: "OFFLINE_ACCOUNT_MISMATCH" });
  await storage.setUncommittedVisitStatus(1, "DONE", undefined, account);
  expect(
    await storage.committedVisitStatus("1", snapshot[1], undefined, account),
  ).toBe(false);
  expect(
    await storage.committedVisitStatus("2", snapshot[2], undefined, account),
  ).toBe(true);
  expect(Object.keys(await storage.getUncommittedVisitStatus())).toEqual(["1"]);
  expect((await storage.getUncommittedVisitStatus())[1].status).toBe("DONE");
});

it("rejects stale-screen enqueues after a safe account switch", async () => {
  await storage.activateOfflineAccount("https://synthetic.invalid|2");
  await expect(
    storage.addOfflineBaby({ name: "Synthetic old screen" }, account),
  ).rejects.toMatchObject({ code: "OFFLINE_ACCOUNT_MISMATCH" });
  expect(await storage.pendingOfflineRecords()).toBe(0);
});

it("retains questionnaire answers changed while a visit acknowledgement was in flight", async () => {
  await storage.setUncommittedVisitStatus(
    1,
    "DONE",
    "2026-11-12T10:00:00",
    account,
  );
  await storage.setAnswers(1, { answers: ["Synthetic old answer"] }, account);
  const snapshot = (await storage.getUncommittedVisitStatus())[1];
  const oldAnswers = await storage.getAnswers(1);
  await storage.setAnswers(1, { answers: ["Synthetic new answer"] }, account);
  expect(
    await storage.committedVisitStatus("1", snapshot, oldAnswers, account),
  ).toBe(false);
  expect((await storage.getAnswers(1)).answers).toEqual([
    "Synthetic new answer",
  ]);
  expect((await storage.getUncommittedVisitStatus())[1]).toEqual(snapshot);
  await expect(
    storage.activateOfflineAccount("https://synthetic.invalid|2"),
  ).rejects.toMatchObject({ code: "OFFLINE_ACCOUNT_MISMATCH" });
});

it("rejects an old account acknowledgement after another account reuses the visit ID", async () => {
  const start = "2026-11-12T10:00:00";
  await storage.setUncommittedVisitStatus(7, "UNDONE", start, account);
  const snapshot = (await storage.getUncommittedVisitStatus())[7];
  expect(
    await storage.committedVisitStatus("7", snapshot, undefined, account),
  ).toBe(true);
  const nextAccount = "https://synthetic.invalid|2";
  await storage.activateOfflineAccount(nextAccount);
  await storage.setUncommittedVisitStatus(7, "UNDONE", start, nextAccount);
  const replacement = (await storage.getUncommittedVisitStatus())[7];
  expect(
    await storage.committedVisitStatus("7", snapshot, undefined, account),
  ).toBe(false);
  expect((await storage.getUncommittedVisitStatus())[7]).toEqual(replacement);
});

it("rejects duplicate acknowledgement after the same account recreates identical visit work", async () => {
  const start = "2026-11-12T10:00:00";
  await storage.setUncommittedVisitStatus(7, "UNDONE", start, account);
  const snapshot = (await storage.getUncommittedVisitStatus())[7];
  await storage.committedVisitStatus("7", snapshot, undefined, account);
  await storage.setUncommittedVisitStatus(7, "UNDONE", start, account);
  const replacement = (await storage.getUncommittedVisitStatus())[7];
  expect(replacement._offlineRevision).not.toBe(snapshot._offlineRevision);
  expect(
    await storage.committedVisitStatus("7", snapshot, undefined, account),
  ).toBe(false);
  expect((await storage.getUncommittedVisitStatus())[7]).toEqual(replacement);
});

it("requires an authenticated account even when a storage owner exists", async () => {
  await expect(storage.requireOfflineAccount(null)).rejects.toMatchObject({
    code: "OFFLINE_ACCOUNT_MISMATCH",
  });
  await expect(storage.requireOfflineAccount(undefined)).rejects.toMatchObject({
    code: "OFFLINE_ACCOUNT_MISMATCH",
  });
});
