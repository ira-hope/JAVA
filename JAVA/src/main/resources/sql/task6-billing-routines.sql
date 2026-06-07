-- Task 6: Database Routines and Messaging
-- On bill generation: insert notification message
-- On full payment: update bill status and notify customer
-- Statements end with ;; (see spring.sql.init.separator in application.properties)

CREATE OR REPLACE FUNCTION fn_build_bill_message(p_bill_id BIGINT)
RETURNS TEXT
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_full_name TEXT;
    v_period TEXT;
    v_amount NUMERIC(12, 2);
BEGIN
    SELECT c.full_name,
           trim(to_char(make_date(bc.billing_year, bc.billing_month, 1), 'FMMonth')) || '/' || bc.billing_year,
           b.total_amount
    INTO v_full_name, v_period, v_amount
    FROM bills b
    JOIN meters m ON m.id = b.meter_id
    JOIN customers c ON c.id = m.customer_id
    JOIN billing_cycles bc ON bc.id = b.billing_cycle_id
    WHERE b.id = p_bill_id;

    IF v_full_name IS NULL THEN
        RETURN NULL;
    END IF;

    RETURN 'Dear ' || v_full_name || E',\n'
        || 'Your ' || v_period || ' utility bill of ' || v_amount || ' FRW has been successfully processed.';
END;
$$;;

CREATE OR REPLACE FUNCTION trg_bill_insert_notify_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_message TEXT;
    v_customer_id BIGINT;
    v_email TEXT;
BEGIN
    v_message := fn_build_bill_message(NEW.id);
    IF v_message IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT c.id, c.email
    INTO v_customer_id, v_email
    FROM meters m
    JOIN customers c ON c.id = m.customer_id
    WHERE m.id = NEW.meter_id;

    INSERT INTO notifications (customer_id, message, status, recipient_email, read, created_at, updated_at)
    VALUES (v_customer_id, v_message, 'PENDING', v_email, false, NOW(), NOW());

    RETURN NEW;
END;
$$;;

DROP TRIGGER IF EXISTS trg_bill_insert_notify ON bills;;

CREATE TRIGGER trg_bill_insert_notify
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION trg_bill_insert_notify_fn();;

CREATE OR REPLACE PROCEDURE sp_process_payment(p_bill_id BIGINT, p_amount_paid NUMERIC)
LANGUAGE plpgsql
AS $$
DECLARE
    v_bill bills%ROWTYPE;
    v_message TEXT;
    v_customer_id BIGINT;
    v_email TEXT;
    v_new_outstanding NUMERIC(12, 2);
BEGIN
    SELECT * INTO v_bill FROM bills WHERE id = p_bill_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Bill not found: %', p_bill_id;
    END IF;

    v_new_outstanding := v_bill.outstanding_amount - p_amount_paid;

    IF v_new_outstanding < 0 THEN
        RAISE EXCEPTION 'Payment amount exceeds outstanding balance of %', v_bill.outstanding_amount;
    END IF;

    IF v_new_outstanding = 0 THEN
        UPDATE bills
        SET outstanding_amount = 0, status = 'PAID', updated_at = NOW()
        WHERE id = p_bill_id;

        v_message := fn_build_bill_message(p_bill_id);

        SELECT c.id, c.email
        INTO v_customer_id, v_email
        FROM meters m
        JOIN customers c ON c.id = m.customer_id
        WHERE m.id = v_bill.meter_id;

        INSERT INTO notifications (customer_id, message, status, recipient_email, read, created_at, updated_at)
        VALUES (v_customer_id, v_message, 'PENDING', v_email, false, NOW(), NOW());
    ELSE
        UPDATE bills
        SET outstanding_amount = v_new_outstanding, status = 'PARTIALLY_PAID', updated_at = NOW()
        WHERE id = p_bill_id;
    END IF;
END;
$$;;

CREATE OR REPLACE FUNCTION trg_payment_insert_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    CALL sp_process_payment(NEW.bill_id, NEW.amount_paid);
    RETURN NEW;
END;
$$;;

DROP TRIGGER IF EXISTS trg_payment_insert ON payments;;

CREATE TRIGGER trg_payment_insert
    AFTER INSERT ON payments
    FOR EACH ROW
    EXECUTE FUNCTION trg_payment_insert_fn();;
