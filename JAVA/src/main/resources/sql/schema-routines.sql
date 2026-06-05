-- WASAC Utility Billing System: optional PostgreSQL views, functions, and procedures.
-- Database routines support (run manually in PostgreSQL when needed)

-- Example view: active users
CREATE OR REPLACE VIEW v_active_users AS
SELECT id, email, first_name, last_name, enabled, email_verified, created_at, updated_at
FROM users
WHERE enabled = true;

-- Example function: count active users
CREATE OR REPLACE FUNCTION fn_count_active_users()
RETURNS BIGINT
LANGUAGE sql
STABLE
AS $$
    SELECT COUNT(*) FROM users WHERE enabled = true;
$$;

-- Example procedure: permanently delete user by email
CREATE OR REPLACE PROCEDURE sp_delete_user(p_email VARCHAR)
LANGUAGE plpgsql
AS $$
DECLARE
    v_user_id BIGINT;
BEGIN
    SELECT id INTO v_user_id FROM users WHERE LOWER(email) = LOWER(p_email);
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'User not found: %', p_email;
    END IF;
    DELETE FROM refresh_tokens WHERE user_id = v_user_id;
    DELETE FROM user_roles WHERE user_id = v_user_id;
    DELETE FROM users WHERE id = v_user_id;
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
