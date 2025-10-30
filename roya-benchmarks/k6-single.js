import http from 'k6/http';
import { sleep } from 'k6';
export let options = { vus: Number(__ENV.VUS||100), duration: __ENV.DURATION||'30s' };
export default function () {
  const base = __ENV.BASE_URL || 'http://localhost:3101';
  const path = __ENV.PATH || '/owners';
  http.get(base + path);
  sleep(1);
}
