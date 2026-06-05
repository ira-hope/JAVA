-- WASAC Utility Billing System: drops all tables so the schema can be recreated cleanly.
-- Drops all application tables so Hibernate can recreate them on next startup.
-- Usage (PowerShell):
--   $env:PGPASSWORD='password'
--   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d wasac -f "src\main\resources\sql\reset-schema.sql"

DROP VIEW IF EXISTS v_active_users CASCADE;
DROP PROCEDURE IF EXISTS sp_soft_delete_user(VARCHAR);
DROP PROCEDURE IF EXISTS sp_delete_user(VARCHAR);
DROP FUNCTION IF EXISTS fn_count_active_users();
DROP FUNCTION IF EXISTS trg_users_audit_fn() CASCADE;

DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS otp_verifications CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
