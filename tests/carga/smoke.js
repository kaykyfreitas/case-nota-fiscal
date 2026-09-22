import http from "k6/http";
import { check } from "k6";
import { baseUrl, headers, pfPayload, pjSimplesPayload } from "./payloads.js";

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const pf = http.post(`${baseUrl()}/api/pedido/gerarNotaFiscal`, pfPayload(), {
    headers: headers(),
  });
  check(pf, {
    "status 200": (r) => r.status === 200,
    "um item": (r) => {
      try {
        return r.json("itens").length === 1;
      } catch (e) {
        return false;
      }
    },
    "tributo da linha PF": (r) => {
      try {
        return r.json("itens.0.valor_tributo_item") === 0;
      } catch (e) {
        return false;
      }
    },
  });

  const pj = http.post(`${baseUrl()}/api/pedido/gerarNotaFiscal`, pjSimplesPayload(), {
    headers: headers(),
  });
  check(pj, {
    "status 200 PJ": (r) => r.status === 200,
    "tributo da linha PJ": (r) => {
      try {
        return r.json("itens.0.valor_tributo_item") === 1109.6;
      } catch (e) {
        return false;
      }
    },
  });
}
