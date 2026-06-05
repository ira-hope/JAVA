-- Fix users.requested_role check constraint after adding ROLE_ADMIN to the application enum.
-- Run manually if startup migration does not apply:
--   psql -U postgres -d ubsdatabase -f src/main/resources/sql/fix-requested-role-constraint.sql

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_requested_role_check;

ALTER TABLE users ADD CONSTRAINT users_requested_role_check
CHECK (requested_role IN (
    'ROLE_ADMIN',
    'ROLE_CUSTOMER',
    'ROLE_OPERATOR',
    'ROLE_FINANCE'
));
