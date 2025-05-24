So for the Database to work you need following SQL script:
-- Table: public.tokens

-- DROP TABLE IF EXISTS public.tokens;

CREATE TABLE IF NOT EXISTS public.tokens
(
    email character varying(36) COLLATE pg_catalog."default",
    password character varying(36) COLLATE pg_catalog."default",
    id character varying(36) COLLATE pg_catalog."default",
    token character varying(36) COLLATE pg_catalog."default"
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.tokens
    OWNER to postgres;
-----------------------------------------------------------------------------
-- Table: public.contacts

-- DROP TABLE IF EXISTS public.contacts;

CREATE TABLE IF NOT EXISTS public.contacts
(
    owner_email character varying(100) COLLATE pg_catalog."default" NOT NULL,
    contact_email character varying(100) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT contacts_pkey PRIMARY KEY (owner_email, contact_email),
    CONSTRAINT contacts_contact_email_fkey FOREIGN KEY (contact_email)
        REFERENCES public.users (email) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT contacts_owner_email_fkey FOREIGN KEY (owner_email)
        REFERENCES public.users (email) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.contacts
    OWNER to postgres;
-----------------------------------------------------------------------------
-- Table: public.balances

-- DROP TABLE IF EXISTS public.balances;

CREATE TABLE IF NOT EXISTS public.balances
(
    email character varying(255) COLLATE pg_catalog."default" NOT NULL,
    balance numeric(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT user_balances_pkey PRIMARY KEY (email),
    CONSTRAINT user_balances_user_email_fkey FOREIGN KEY (email)
        REFERENCES public.users (email) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.balances
    OWNER to postgres;
-----------------------------------------------------------------------------
-- Table: public.transactions

-- DROP TABLE IF EXISTS public.transactions;

CREATE TABLE IF NOT EXISTS public.transactions
(
    amount integer,
    description character varying(255) COLLATE pg_catalog."default",
    "timestamp" bigint,
    recipient_email character varying(50) COLLATE pg_catalog."default",
    sender_email character varying(50) COLLATE pg_catalog."default"
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.transactions
    OWNER to postgres;
-----------------------------------------------------------------------------
-- Table: public.users

-- DROP TABLE IF EXISTS public.users;

CREATE TABLE IF NOT EXISTS public.users
(
    password character varying(100) COLLATE pg_catalog."default",
    email character varying(100) COLLATE pg_catalog."default",
    firstname character varying(100) COLLATE pg_catalog."default",
    secondname character varying(100) COLLATE pg_catalog."default",
    CONSTRAINT users_email_unique UNIQUE (email)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.users
    OWNER to postgres;

-- Trigger: after_user_insert

-- DROP TRIGGER IF EXISTS after_user_insert ON public.users;

CREATE OR REPLACE TRIGGER after_user_insert
    AFTER INSERT
    ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION public.create_balance_for_new_user();