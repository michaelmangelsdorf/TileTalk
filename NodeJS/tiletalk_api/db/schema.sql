SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: schema_migrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schema_migrations (
    version character varying NOT NULL
);


--
-- Name: sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sessions (
    sid text NOT NULL,
    sess jsonb,
    expire timestamp without time zone
);


--
-- Name: tt_contacts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tt_contacts (
    id integer NOT NULL,
    requester_id integer,
    target_id integer,
    authorized integer DEFAULT 0
);


--
-- Name: tt_contacts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tt_contacts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tt_contacts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tt_contacts_id_seq OWNED BY public.tt_contacts.id;


--
-- Name: tt_cryptograms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tt_cryptograms (
    id integer NOT NULL,
    message_id integer,
    recipient_id integer,
    payload jsonb,
    seen boolean DEFAULT false NOT NULL
);


--
-- Name: tt_cryptograms_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tt_cryptograms_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tt_cryptograms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tt_cryptograms_id_seq OWNED BY public.tt_cryptograms.id;


--
-- Name: tt_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tt_messages (
    id integer NOT NULL,
    tile_id integer,
    responder_id integer,
    created_at timestamp with time zone DEFAULT now()
);


--
-- Name: tt_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tt_messages_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tt_messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tt_messages_id_seq OWNED BY public.tt_messages.id;


--
-- Name: tt_tiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tt_tiles (
    id integer NOT NULL,
    owner_id integer,
    x_coord integer NOT NULL,
    y_coord integer NOT NULL,
    starter_id integer,
    symbol text,
    animation_type integer DEFAULT 0,
    flip boolean DEFAULT false,
    tile_bg numeric DEFAULT 0,
    callout text,
    title text
);


--
-- Name: tt_tiles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tt_tiles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tt_tiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tt_tiles_id_seq OWNED BY public.tt_tiles.id;


--
-- Name: tt_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tt_users (
    id integer NOT NULL,
    username text NOT NULL,
    password_hash text NOT NULL,
    public_key text
);


--
-- Name: tt_users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tt_users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tt_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tt_users_id_seq OWNED BY public.tt_users.id;


--
-- Name: tt_contacts id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_contacts ALTER COLUMN id SET DEFAULT nextval('public.tt_contacts_id_seq'::regclass);


--
-- Name: tt_cryptograms id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_cryptograms ALTER COLUMN id SET DEFAULT nextval('public.tt_cryptograms_id_seq'::regclass);


--
-- Name: tt_messages id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_messages ALTER COLUMN id SET DEFAULT nextval('public.tt_messages_id_seq'::regclass);


--
-- Name: tt_tiles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_tiles ALTER COLUMN id SET DEFAULT nextval('public.tt_tiles_id_seq'::regclass);


--
-- Name: tt_users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_users ALTER COLUMN id SET DEFAULT nextval('public.tt_users_id_seq'::regclass);


--
-- Name: schema_migrations schema_migrations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schema_migrations
    ADD CONSTRAINT schema_migrations_pkey PRIMARY KEY (version);


--
-- Name: sessions sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (sid);


--
-- Name: tt_contacts tt_contacts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_contacts
    ADD CONSTRAINT tt_contacts_pkey PRIMARY KEY (id);


--
-- Name: tt_contacts tt_contacts_requester_id_target_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_contacts
    ADD CONSTRAINT tt_contacts_requester_id_target_id_key UNIQUE (requester_id, target_id);


--
-- Name: tt_cryptograms tt_cryptograms_message_id_recipient_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_cryptograms
    ADD CONSTRAINT tt_cryptograms_message_id_recipient_id_key UNIQUE (message_id, recipient_id);


--
-- Name: tt_cryptograms tt_cryptograms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_cryptograms
    ADD CONSTRAINT tt_cryptograms_pkey PRIMARY KEY (id);


--
-- Name: tt_messages tt_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_messages
    ADD CONSTRAINT tt_messages_pkey PRIMARY KEY (id);


--
-- Name: tt_messages tt_messages_tile_id_responder_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_messages
    ADD CONSTRAINT tt_messages_tile_id_responder_id_key UNIQUE (tile_id, responder_id);


--
-- Name: tt_tiles tt_tiles_owner_id_x_coord_y_coord_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_tiles
    ADD CONSTRAINT tt_tiles_owner_id_x_coord_y_coord_key UNIQUE (owner_id, x_coord, y_coord);


--
-- Name: tt_tiles tt_tiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_tiles
    ADD CONSTRAINT tt_tiles_pkey PRIMARY KEY (id);


--
-- Name: tt_users tt_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_users
    ADD CONSTRAINT tt_users_pkey PRIMARY KEY (id);


--
-- Name: tt_contacts tt_contacts_requester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_contacts
    ADD CONSTRAINT tt_contacts_requester_id_fkey FOREIGN KEY (requester_id) REFERENCES public.tt_users(id) ON DELETE CASCADE;


--
-- Name: tt_contacts tt_contacts_target_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_contacts
    ADD CONSTRAINT tt_contacts_target_id_fkey FOREIGN KEY (target_id) REFERENCES public.tt_users(id) ON DELETE CASCADE;


--
-- Name: tt_cryptograms tt_cryptograms_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_cryptograms
    ADD CONSTRAINT tt_cryptograms_message_id_fkey FOREIGN KEY (message_id) REFERENCES public.tt_messages(id) ON DELETE CASCADE;


--
-- Name: tt_cryptograms tt_cryptograms_recipient_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_cryptograms
    ADD CONSTRAINT tt_cryptograms_recipient_id_fkey FOREIGN KEY (recipient_id) REFERENCES public.tt_users(id) ON DELETE CASCADE;


--
-- Name: tt_messages tt_messages_responder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_messages
    ADD CONSTRAINT tt_messages_responder_id_fkey FOREIGN KEY (responder_id) REFERENCES public.tt_users(id) ON DELETE CASCADE;


--
-- Name: tt_messages tt_messages_tile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_messages
    ADD CONSTRAINT tt_messages_tile_id_fkey FOREIGN KEY (tile_id) REFERENCES public.tt_tiles(id) ON DELETE CASCADE;


--
-- Name: tt_tiles tt_tiles_owner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_tiles
    ADD CONSTRAINT tt_tiles_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.tt_users(id) ON DELETE CASCADE;


--
-- Name: tt_tiles tt_tiles_starter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tt_tiles
    ADD CONSTRAINT tt_tiles_starter_id_fkey FOREIGN KEY (starter_id) REFERENCES public.tt_users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--


--
-- Dbmate schema migrations
--

INSERT INTO public.schema_migrations (version) VALUES
    ('20250727135824'),
    ('20250727135839'),
    ('20250727148850'),
    ('20250727154826'),
    ('20250728053108'),
    ('20250728092628'),
    ('20250804141650'),
    ('20250808052402'),
    ('20250808093346');
