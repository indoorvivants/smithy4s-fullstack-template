import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  plugins: [
    scalaJSPlugin({
      // path to the directory containing the sbt build
      // default: '.'
      cwd: "../..",

      // sbt project ID from within the sbt build to get fast/fullLinkJS from
      // default: the root project of the sbt build
      projectID: "frontend",

      // URI prefix of imports that this plugin catches (without the trailing ':')
      // default: 'scalajs' (so the plugin recognizes URIs starting with 'scalajs:')
      uriPrefix: "scalajs",
    }),
  ],
  server: {
    proxy: {
      "/api": {
        target: `http://localhost:${process.env.BACKEND_PORT || 8080}`,
        changeOrigin: true,
      },
    },
  },
});
