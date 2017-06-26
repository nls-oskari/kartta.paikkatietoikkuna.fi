-- There might be duplicate usernames in preauth
ALTER TABLE oskari_users DROP CONSTRAINT IF EXISTS oskari_users_user_name_key;
-- email should be unique
ALTER TABLE oskari_users DROP CONSTRAINT IF EXISTS oskari_users_email_key;
ALTER TABLE oskari_users ADD CONSTRAINT oskari_users_email_key UNIQUE (email);