import path from "path";
import {fileURLToPath} from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname  = path.dirname(__filename);

const config = {
  entry: "./out/javascripts/Main.js"
, mode:  "production"
, output: {
    filename: "realmain.js"
  , path:     path.resolve(__dirname, "out/javascripts")
  }
};

export default config;
