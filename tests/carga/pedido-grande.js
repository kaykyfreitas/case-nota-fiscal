import http from "k6/http";
import { check } from "k6";
import { baseUrl, headers, pedidoGrandePayload } from "./payloads.js";

export const options = {
  vus: 1,
  iterations: 3,
};

export default function () {
  const res = http.post(`${baseUrl()}/api/pedido/gerarNotaFiscal`, pedidoGrandePayload(), {
    headers: headers(),
  });
  check(res, {
    "status 200": (r) => r.status === 200,
    "seis itens": (r) => {
      try {
        return r.json("itens").length === 6;
      } catch (e) {
        return false;
      }
    },
  });
}
