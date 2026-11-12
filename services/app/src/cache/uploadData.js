import storage from "./storage";
import http from "../utils/http";

let syncInProgress;

async function uploadQueues() {
  const session = await http.session();
  if (!session.account || !session.token) return { uploaded: 0, held: true };
  await storage.requireOfflineAccount(session.account);
  let uploaded = 0;
  // Acknowledge families before proceeding to dependent visit work.
  for (const queue of [
    {
      read: storage.getOfflineBabies,
      acknowledge: storage.acknowledgeOfflineBaby,
      url: "/api/babies",
    },
    {
      read: storage.getOfflineVisits,
      acknowledge: storage.acknowledgeOfflineVisit,
      url: "/api/visits",
    },
  ]) {
    const records = (await queue.read()) || [];
    for (const record of records) {
      const current = await http.session();
      if (
        current.account !== session.account ||
        current.token !== session.token
      ) {
        return { uploaded, held: true };
      }
      if (!record._offlineId) return { uploaded, held: true };
      const { _offlineId, ...payload } = record;
      try {
        // An in-flight request must keep its original account credentials.
        await http.post(queue.url, payload, { token: session.token });
        await queue.acknowledge(_offlineId, session.account);
        uploaded++;
      } catch (_) {
        // Preserve this entry and remaining work for a later retry. A server
        // commit with a lost response still needs server-side idempotency.
        return { uploaded, failed: true };
      }
    }
  }
  return { uploaded };
}

export function syncOfflineData() {
  if (!syncInProgress) {
    syncInProgress = uploadQueues().finally(() => {
      syncInProgress = undefined;
    });
  }
  return syncInProgress;
}

export const uploadOfflineBabies = syncOfflineData;
export const uploadOfflineVisits = syncOfflineData;
