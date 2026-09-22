import http from "k6/http";
import { check } from "k6";
import { baseUrl, headers, pfPayload } from "./payloads.js";

export const options = {
  vus: 8,
  duration: "30s",
};

export default function () {
  const res = http.post(`${baseUrl()}/api/pedido/gerarNotaFiscal`, pfPayload(), {
    headers: headers(),
  });
  check(res, {
    "status 200": (r) => r.status === 200,
    "um item": (r) => {
      try {
        return r.json("itens").length === 1;
      } catch (e) {
        return false;
      }
    },
    "tributo da linha": (r) => {
      try {
        return r.json("itens.0.valor_tributo_item") === 0;
      } catch (e) {
        return false;
      }
    },
  });
}
