-- Database routines support (run manually in PostgreSQL when needed)

-- Example view: active users
CREATE OR REPLACE VIEW v_active_users AS
SELECT id, email, first_name, last_name, enabled, email_verified, created_at, updated_at
FROM users
WHERE deleted = false;

-- Example function: count active users
CREATE OR REPLACE FUNCTION fn_count_active_users()
RETURNS BIGINT
LANGUAGE sql
STABLE
AS $$
    SELECT COUNT(*) FROM users WHERE deleted = false;
$$;

-- Example procedure: soft-delete user by email
CREATE OR REPLACE PROCEDURE sp_soft_delete_user(p_email VARCHAR)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE users
    SET deleted = true, enabled = false, updated_at = NOW()
    WHERE email = p_email AND deleted = false;
END;
$$;

-- Example trigger function: audit user updates
CREATE OR REPLACE FUNCTION trg_users_audit_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO audit_logs (action, entity_type, entity_id, performed_by, details, created_at, updated_at)
    VALUES ('UPDATE', 'User', NEW.id, NEW.email, 'User record updated', NOW(), NOW());
    RETURN NEW;
END;
$$;

-- CREATE TRIGGER trg_users_audit
-- AFTER UPDATE ON users
-- FOR EACH ROW EXECUTE FUNCTION trg_users_audit_fn();
