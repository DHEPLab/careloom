const productionEndpoint =
  require("./eas.json").build.production.env.EXPO_PUBLIC_API_URL;

function validateApiEndpoint(profile, endpoint) {
  let parsed;
  try {
    parsed = new URL(endpoint);
  } catch (_) {
    throw new Error(
      "Set EXPO_PUBLIC_API_URL explicitly to your isolated CareLoom API endpoint before building.",
    );
  }
  if (
    !["http:", "https:"].includes(parsed.protocol) ||
    parsed.username ||
    parsed.password
  ) {
    throw new Error(
      "EXPO_PUBLIC_API_URL must be an HTTP(S) endpoint without embedded credentials.",
    );
  }
  if (
    profile !== "production" &&
    parsed.hostname === new URL(productionEndpoint).hostname
  ) {
    throw new Error(
      "Development and preview builds must use an isolated API endpoint, not the production deployment.",
    );
  }
  return endpoint;
}
module.exports = { validateApiEndpoint };
