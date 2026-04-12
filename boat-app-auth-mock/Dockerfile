# ─── Auth-Mock Placeholder ───────────────────────────────────────────────────
# This module does not yet exist. This Dockerfile keeps a healthy container
# running on port 9000 so that docker-compose depends_on conditions are met.
# It will be replaced when the real auth module is implemented.
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'echo "Auth mock placeholder — module not yet implemented"' >> /app/start.sh && \
    echo 'while true; do sleep 30; done' >> /app/start.sh && \
    chmod +x /app/start.sh

EXPOSE 9000

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD echo "ok" || exit 1

ENTRYPOINT ["/app/start.sh"]

