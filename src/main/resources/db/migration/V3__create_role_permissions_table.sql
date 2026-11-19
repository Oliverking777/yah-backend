-- Flyway migration V3: join table linking roles and permissions (many-to-many).
-- Never edit this file after it has been applied; add a new V4__... file for changes.

CREATE TABLE public.role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    -- deleting a role removes its links
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES public.roles (role_id) ON DELETE CASCADE,
    -- a permission that is still assigned to a role cannot be deleted
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES public.permissions (permission_id) ON DELETE RESTRICT
);

-- the primary key already covers lookups by role_id; this one covers "which roles use this permission?"
CREATE INDEX idx_role_permissions_permission_id ON public.role_permissions (permission_id);

COMMENT ON TABLE public.role_permissions IS 'Links roles to the permissions they grant';
