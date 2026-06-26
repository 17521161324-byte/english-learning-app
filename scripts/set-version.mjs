import { readFileSync, writeFileSync } from "node:fs";

const versionCode = process.env.APP_VERSION_CODE;
const versionName = process.env.APP_VERSION_NAME;

if (!versionCode || !versionName) {
  throw new Error("APP_VERSION_CODE and APP_VERSION_NAME are required.");
}

const appPath = new URL("../app.js", import.meta.url);
let source = readFileSync(appPath, "utf8");

source = source
  .replace(/const APP_VERSION_CODE = \d+;/, `const APP_VERSION_CODE = ${Number(versionCode)};`)
  .replace(/const APP_VERSION_NAME = "[^"]+";/, `const APP_VERSION_NAME = "${versionName}";`);

writeFileSync(appPath, source);

