import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
  vus: 100,
  duration: '30s'
};

export default function () {
  http.get('http://localhost:3101/owners'); // Roya
  http.get('http://localhost:8070/owners'); // Spring
  sleep(1);
}

