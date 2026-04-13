-- Seed data 10 boats for dev profile
-- Loaded on every startup because spring.sql.init.mode=always + ddl-auto=create-drop.

INSERT INTO boat (id, name, description, created_at) VALUES
  (nextval('boat_seq'), 'Aurora',       'A graceful sailing yacht designed for long ocean passages.',  NOW() - INTERVAL '30 days'),
  (nextval('boat_seq'), 'Blue Horizon', 'A robust offshore cruiser built for heavy weather sailing.',  NOW() - INTERVAL '27 days'),
  (nextval('boat_seq'), 'Calypso',      'A classic wooden sloop, lovingly restored to original glory.', NOW() - INTERVAL '24 days'),
  (nextval('boat_seq'), 'Drifter',      'A lightweight racing dinghy with exceptional upwind performance.', NOW() - INTERVAL '20 days'),
  (nextval('boat_seq'), 'Eclipse',      'A modern catamaran offering stability and spacious living quarters.', NOW() - INTERVAL '17 days'),
  (nextval('boat_seq'), 'Fair Wind',    'A traditional ketch rig ideal for single-handed ocean voyages.', NOW() - INTERVAL '14 days'),
  (nextval('boat_seq'), 'Gale Runner',  'A high-performance offshore racer engineered for speed.',       NOW() - INTERVAL '10 days'),
  (nextval('boat_seq'), 'Harbour Light','A tender motorboat used for marina transfers and short trips.',  NOW() - INTERVAL '7 days'),
  (nextval('boat_seq'), 'Iron Maiden',  'A sturdy steel hull expedition vessel with ice-class rating.',  NOW() - INTERVAL '3 days'),
  (nextval('boat_seq'), 'Jade Pearl',   'A luxury motor yacht equipped with all modern amenities.',      NOW() - INTERVAL '1 day');
