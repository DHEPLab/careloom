import { useState, useEffect, useRef } from "react";
import moment from "moment";
import AsyncStorage from "@react-native-async-storage/async-storage";

function addObject(key, obj) {
  return AsyncStorage.setItem(key, JSON.stringify(obj));
}

function addValue(key, value) {
  return AsyncStorage.setItem(key, value);
}

function getObject(key) {
  return new Promise(async (resolve, reject) => {
    try {
      const json = await AsyncStorage.getItem(key);
      resolve(json ? JSON.parse(json) : null);
    } catch (e) {
      reject(e);
    }
  });
}

function getValue(key) {
  return new Promise(async (resolve, reject) => {
    try {
      resolve((await AsyncStorage.getItem(key)) || 0);
    } catch (e) {
      reject(e);
    }
  });
}

function getLesson(id) {
  return getObject(`LESSON_${id}`);
}

function getModule(id) {
  return getObject(`MODULE_${id}`);
}

function getNextShouldVisit(id) {
  return getObject(`NEXT_VISIT_${id}`);
}

function getNextVisit() {
  return getObject(`NEXT_VISIT`);
}

function getBabies() {
  return getObject(`BABIES`);
}

function getOfflineBabies() {
  return getObject("OFFLINEBABIES");
}

function getOfflineVisits() {
  return getObject("OFFLINE_VISITS");
}

// Serialize queue mutations, including account transitions and acknowledgements.
// Screen read/replace operations can overwrite work saved during an upload.
let queueWrite = Promise.resolve();
function changeQueue(action) {
  const result = queueWrite.then(action);
  queueWrite = result.catch(() => {});
  return result;
}

async function pendingOfflineRecords() {
  const answerKeys = (await AsyncStorage.getAllKeys()).filter((key) =>
    key.startsWith("ANSWER_LESSON_"),
  );
  const answers = await Promise.all(answerKeys.map(getObject));
  const pendingAnswers = answers.filter(
    (answer) => answer && Object.keys(answer).length > 0,
  ).length;
  return (
    ((await getOfflineBabies()) || []).length +
    ((await getOfflineVisits()) || []).length +
    Object.keys((await getUncommittedVisitStatus()) || {}).length +
    pendingAnswers
  );
}

function ownershipError(code = "OFFLINE_ACCOUNT_MISMATCH") {
  const error = new Error(
    "Pending offline records require the original account or administrator recovery.",
  );
  error.code = code;
  return error;
}

async function requireOfflineAccount(account) {
  const owner = await getValue("OFFLINE_ACCOUNT");
  if (!account || !owner || owner !== account) throw ownershipError();
  return owner;
}

async function clearAccountCache() {
  const keys = await AsyncStorage.getAllKeys();
  const privateKeys = keys.filter(
    (key) =>
      [
        "BABIES",
        "NEXT_VISIT",
        "NEXT_MODULE",
        "LAST_UPDATE_AT",
        "UNCOMMITTED_VISIT_STATUS",
      ].includes(key) ||
      /^(LESSON_|MODULE_|NEXT_VISIT_|NEXT_MODULE_|ANSWER_LESSON_)/.test(key),
  );
  await AsyncStorage.multiRemove(privateKeys);
}

function activateOfflineAccount(account) {
  return changeQueue(async () => {
    if (!account) throw ownershipError();
    const owner = await getValue("OFFLINE_ACCOUNT");
    if (owner === account) return;
    // Legacy queues have no trustworthy owner. Keep their original bytes held.
    if (await pendingOfflineRecords()) {
      throw ownershipError(
        owner ? "OFFLINE_ACCOUNT_MISMATCH" : "OFFLINE_OWNER_UNKNOWN",
      );
    }
    await clearAccountCache();
    await addValue("OFFLINE_ACCOUNT", account);
  });
}

function appendOfflineRecord(key, record, account) {
  return changeQueue(async () => {
    if (!account) throw ownershipError();
    await requireOfflineAccount(account);
    const records = (await getObject(key)) || [];
    const entry = {
      ...record,
      _offlineId: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    };
    await addObject(key, [...records, entry]);
    return entry;
  });
}

function acknowledgeOfflineRecord(key, id, account) {
  return changeQueue(async () => {
    await requireOfflineAccount(account);
    const records = (await getObject(key)) || [];
    await addObject(
      key,
      records.filter((record) => record._offlineId !== id),
    );
  });
}

function getAnswers(id) {
  return getObject(`ANSWER_LESSON_${id}`);
}

function getNextModule(visitId) {
  if (visitId === undefined || visitId === null) return Promise.resolve(0);
  return getValue(`NEXT_MODULE_${visitId}`);
}

function getUncommittedVisitStatus() {
  return getObject(`UNCOMMITTED_VISIT_STATUS`);
}

function useNumber(getFn, id) {
  const [value, setValue] = useState({ id, value: 0 });
  const currentId = useRef(id);
  currentId.current = id;
  function load() {
    return getFn(id).then((data) => {
      if (currentId.current === id) setValue({ id, value: Number(data) || 0 });
    });
  }
  useEffect(() => {
    load();
  }, [id]);
  return [value.id === id ? value.value : 0, load];
}

function useWrapper(getFn, id) {
  const [value, setValue] = useState({});
  function load(_id) {
    getFn(_id || id).then((data) => {
      setValue(data || {});
    });
  }
  useEffect(() => {
    load();
  }, []);
  return [value, load, setValue];
}

export default {
  addLesson: (lesson) => addObject(`LESSON_${lesson.id}`, lesson),
  addModule: (module) => addObject(`MODULE_${module.id}`, module),
  addNextShouldVisit: (id, visit) => addObject(`NEXT_VISIT_${id}`, visit),
  addOfflineVisit: (visit, account) =>
    appendOfflineRecord("OFFLINE_VISITS", visit, account),
  acknowledgeOfflineVisit: (id, account) =>
    acknowledgeOfflineRecord("OFFLINE_VISITS", id, account),
  setBabies: (babies) => addObject("BABIES", babies),
  addOfflineBaby: (baby, account) =>
    appendOfflineRecord("OFFLINEBABIES", baby, account),
  acknowledgeOfflineBaby: (id, account) =>
    acknowledgeOfflineRecord("OFFLINEBABIES", id, account),
  activateOfflineAccount,
  requireOfflineAccount,
  pendingOfflineRecords,
  setAnswers: (visitId, answer, account) =>
    changeQueue(async () => {
      if (!account) throw ownershipError();
      await requireOfflineAccount(account);
      await addObject(`ANSWER_LESSON_${visitId}`, answer);
    }),
  setNextVisit: (visit) => addObject("NEXT_VISIT", visit),
  setNextModule: (nextModule, visitId, account) =>
    changeQueue(async () => {
      await requireOfflineAccount(account);
      if (visitId === undefined || visitId === null)
        throw new Error("Module progress requires a visit ID.");
      const index = Number(nextModule);
      if (!Number.isInteger(index) || index < 0)
        throw new Error("Module progress must be a nonnegative integer.");
      const statuses = (await getUncommittedVisitStatus()) || {};
      if (statuses[visitId]) {
        statuses[visitId]._offlineRevision =
          `${Date.now()}-${Math.random().toString(36).slice(2)}`;
        await addObject("UNCOMMITTED_VISIT_STATUS", statuses);
      }
      await addValue(`NEXT_MODULE_${visitId}`, index.toString());
    }),
  setUncommittedVisitStatus: (id, status, startTime, account) => {
    return changeQueue(async () => {
      if (!account) throw ownershipError();
      await requireOfflineAccount(account);
      const obj = (await getUncommittedVisitStatus()) || {};
      obj[id] = {
        status,
        startTime: startTime || obj[id]?.startTime,
        _offlineRevision: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
      };
      await addObject("UNCOMMITTED_VISIT_STATUS", obj);
    });
  },
  committedVisitStatus: (
    id,
    acknowledgedStatus,
    acknowledgedAnswers,
    account,
  ) =>
    changeQueue(async () => {
      if (!account || (await getValue("OFFLINE_ACCOUNT")) !== account)
        return false;
      const statuses = (await getUncommittedVisitStatus()) || {};
      if (
        !id ||
        JSON.stringify(statuses[id]) !== JSON.stringify(acknowledgedStatus)
      )
        return false;
      const answers = await getAnswers(id);
      if (
        JSON.stringify(answers ?? null) !==
        JSON.stringify(acknowledgedAnswers ?? null)
      )
        return false;
      await addObject(`ANSWER_LESSON_${id}`, {});
      await AsyncStorage.removeItem(`NEXT_MODULE_${id}`);
      delete statuses[id];
      await addObject("UNCOMMITTED_VISIT_STATUS", statuses);
      return true;
    }),
  setLastUpdateAt: (datetime) =>
    addValue("LAST_UPDATE_AT", moment(datetime).format("YYYY-MM-DDTHH:mm:ss")),
  getLesson,
  getModule,
  getNextShouldVisit,
  getOfflineVisits,
  getNextVisit,
  getBabies,
  getOfflineBabies,
  getAnswers,
  getNextModule,
  getUncommittedVisitStatus,
  getLastUpdateAt: () => getValue("LAST_UPDATE_AT"),
  useLesson: (id) => useWrapper(getLesson, id),
  useModule: (id) => useWrapper(getModule, id),
  useNextShouldVisit: (id) => useWrapper(getNextShouldVisit, id),
  useNextVisit: () => useWrapper(getNextVisit),
  useBabies: () => useWrapper(getBabies),
  useOfflineVisits: () => useWrapper(getOfflineVisits),
  useOfflineBabies: () => useWrapper(getOfflineBabies),
  useAnswers: (id) => useWrapper(getAnswers, id),
  useNextModule: (visitId) => useNumber(getNextModule, visitId),
  useUncommittedVisitStatus: () => useWrapper(getUncommittedVisitStatus),
};
