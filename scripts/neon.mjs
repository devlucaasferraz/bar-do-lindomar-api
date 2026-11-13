import { existsSync, readFileSync } from "node:fs";
import { parseEnv } from "node:util";
import { spawn } from "node:child_process";

const file = process.env.NEON_ENV_FILE ?? ".env.local";
if (existsSync(file)) {
  const values = parseEnv(readFileSync(file, "utf8"));
  for (const [key, value] of Object.entries(values)) process.env[key] ??= value;
}
if (!process.env.DATABASE_URL) {
  console.error("DATABASE_URL ausente. Execute neon deploy ou configure o ambiente do processo.");
  process.exit(1);
}
process.env.SPRING_PROFILES_ACTIVE ??= "prod";
const mode = process.argv[2];
let command;
let args;
if (mode === "test") {
  process.env.NEON_LIVE_TESTS = "true";
  command = "./mvnw";
  args = ["-B", "-Dtest=NeonLiveIntegrationTest", "test"];
} else if (mode === "start") {
  command = "java";
  args = ["-jar", "target/bar-do-lindomar-api-0.0.1-SNAPSHOT.jar", ...process.argv.slice(3)];
} else {
  console.error("Modo inválido. Use npm start ou npm run test:neon.");
  process.exit(1);
}
const child = spawn(command, args, { stdio: "inherit", env: process.env });
for (const signal of ["SIGINT", "SIGTERM"]) process.on(signal, () => child.kill(signal));
child.on("error", () => { console.error("Não foi possível iniciar Java/Maven. Verifique JDK 25 e o build."); process.exitCode = 1; });
child.on("exit", (code, signal) => { process.exitCode = code ?? (signal ? 1 : 0); });
