-- Optional additional constraints (JPA handles most; apply for production hardening)

-- ALTER TABLE users ADD CONSTRAINT chk_users_email_format
--     CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- ALTER TABLE otp_verifications ADD CONSTRAINT chk_otp_not_empty
--     CHECK (char_length(otp) >= 4);
