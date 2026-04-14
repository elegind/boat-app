# ─── Keycloak 24 — Authorization Server ─────────────────────────────────────
# Stage 1: optimised build (providers compiled + realm pre-loaded)
FROM quay.io/keycloak/keycloak:24.0 AS builder

COPY realm-config/boat-realm.json /opt/keycloak/data/import/boat-realm.json
# Custom login theme — reproduces the Angular M3 indigo/teal design
COPY themes/boat-app/ /opt/keycloak/themes/boat-app/

RUN /opt/keycloak/bin/kc.sh build

# Stage 2: lightweight final image
FROM quay.io/keycloak/keycloak:24.0

# Copy the optimised build (includes compiled providers, theme and realm config)
COPY --from=builder /opt/keycloak /opt/keycloak
COPY realm-config/boat-realm.json /opt/keycloak/data/import/boat-realm.json

EXPOSE 9000

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm", "--http-port=9000"]
