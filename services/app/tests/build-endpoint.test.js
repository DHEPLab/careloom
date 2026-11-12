const { validateApiEndpoint } = require("../build-endpoint.cjs");
const production =
  require("../eas.json").build.production.env.EXPO_PUBLIC_API_URL;

test("development and preview require an explicit isolated endpoint", () => {
  expect(() => validateApiEndpoint("preview", undefined)).toThrow(/explicitly/);
  expect(() => validateApiEndpoint("development", production)).toThrow(
    /isolated/,
  );
  expect(() => validateApiEndpoint("preview", production)).toThrow(/isolated/);
  expect(validateApiEndpoint("preview", "https://synthetic.invalid")).toBe(
    "https://synthetic.invalid",
  );
  expect(validateApiEndpoint("production", production)).toBe(production);
});

test("endpoint configuration rejects credentials and non-HTTP protocols", () => {
  expect(() =>
    validateApiEndpoint("preview", "https://user:secret@synthetic.invalid"),
  ).toThrow(/credentials/);
  expect(() => validateApiEndpoint("preview", "file:///tmp/api")).toThrow(
    /HTTP/,
  );
});
