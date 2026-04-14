# ─── Keycloak 24 — Authorization Server ─────────────────────────────────────
# Stage 1 : optimized build (providers + real preloaded)
FROM quay.io/keycloak/keycloak:24.0 AS builder

COPY realm-config/boat-realm.json /opt/keycloak/data/import/boat-realm.json
# Custom theme — reproduces the Angular M3 indigo/teal login design
COPY themes/boat-app/ /opt/keycloak/themes/boat-app/

RUN /opt/keycloak/bin/kc.sh build

# Stage 2 : light final image
FROM quay.io/keycloak/keycloak:24.0

COPY --from=builder /opt/keycloak /opt/keycloak
COPY realm-config/boat-realm.json /opt/keycloak/data/import/boat-realm.json

EXPOSE 9000

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm", "--http-port=9000"]

