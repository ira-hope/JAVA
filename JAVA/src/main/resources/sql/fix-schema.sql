-- Run on existing databases when upgrading the app:
--   psql -U postgres -d ubsdatabase2 -f src/main/resources/sql/fix-schema.sql

-- 1. password_set column (admin-created customer accounts)
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_set boolean DEFAULT true;
UPDATE users SET password_set = true WHERE password_set IS NULL;
ALTER TABLE users ALTER COLUMN password_set SET DEFAULT true;
ALTER TABLE users ALTER COLUMN password_set SET NOT NULL;

-- 2. requested_role check constraint (ROLE_ADMIN support)
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_requested_role_check;
ALTER TABLE users ADD CONSTRAINT users_requested_role_check
CHECK (requested_role IN (
    'ROLE_ADMIN',
    'ROLE_CUSTOMER',
    'ROLE_OPERATOR',
    'ROLE_FINANCE'
));
