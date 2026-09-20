import http from "k6/http";
import { check } from "k6";
import { baseUrl, headers, pfPayload } from "./payloads.js";

export const options = {
  vus: 1,
  iterations: 8,
};

export default function () {
  const res = http.post(`${baseUrl()}/api/pedido/gerarNotaFiscal`, pfPayload(), {
    headers: headers(),
  });
  check(res, {
    "status 200": (r) => r.status === 200,
    "nao acumula itens": (r) => {
      try {
        return r.json("itens").length === 1;
      } catch (e) {
        return false;
      }
    },
  });
}
