CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(32) not null,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER', 'OWNER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Optional case-insensitive uniqueness helper
CREATE UNIQUE INDEX uq_app_user_email_lower
ON app_user (LOWER(email));


-- =========================================
-- 2. WORKSTATION MANAGEMENT
-- =========================================
CREATE TABLE workstation_status (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(9) NULL DEFAULT '#FFFFFF'
);

CREATE TABLE workstation (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    status_id INT NULL REFERENCES workstation_status(id),
    grid_x INT NULL,
    grid_y INT NULL,
    floor INT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Prevent two workstations from using the same position
-- only when all coordinates are actually set
CREATE UNIQUE INDEX uq_workstation_position
ON workstation (floor, grid_x, grid_y)
WHERE floor IS NOT NULL
  AND grid_x IS NOT NULL
  AND grid_y IS NOT NULL;


-- =========================================
-- 3. COMPONENT / ASSET TRACKING
-- =========================================
CREATE TABLE component_category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE component_status (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE component (
    id SERIAL PRIMARY KEY,

    -- Your own internal label, very useful in UI/admin
    asset_tag VARCHAR(50) NOT NULL UNIQUE,

    -- Manufacturer serial is optional and NOT unique
    serial_number VARCHAR(100),

    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(100),

    category_id INT NOT NULL REFERENCES component_category(id),
    status_id INT NOT NULL REFERENCES component_status(id),

    -- Current assignment
    workstation_id INT REFERENCES workstation(id) ON DELETE SET NULL,

    purchase_date DATE,
    warranty_expiry DATE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =========================================
-- 4. COMPONENT ASSIGNMENT HISTORY
-- =========================================
CREATE TABLE component_assignment_log (
    id SERIAL PRIMARY KEY,
    component_id INT NOT NULL REFERENCES component(id) ON DELETE CASCADE,
    workstation_id INT REFERENCES workstation(id) ON DELETE SET NULL,
    assigned_by_user_id INT REFERENCES app_user(id) ON DELETE SET NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    removed_at TIMESTAMPTZ NULL,
    notes TEXT,

    CONSTRAINT chk_assignment_dates
        CHECK (removed_at IS NULL OR removed_at >= assigned_at)
);

-- Only one active assignment record per component
CREATE UNIQUE INDEX uq_component_assignment_log_active
ON component_assignment_log (component_id)
WHERE removed_at IS NULL;


-- =========================================
-- 5. MAINTENANCE
-- =========================================
CREATE TABLE maintenance_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    interval_days INT NOT NULL CHECK (interval_days > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE maintenance_log (
    id SERIAL PRIMARY KEY,
    workstation_id INT NOT NULL REFERENCES workstation(id) ON DELETE CASCADE,
    maintenance_type_id INT NOT NULL REFERENCES maintenance_type(id),
    performed_by_user_id INT REFERENCES app_user(id) ON DELETE SET NULL,
    performed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    notes TEXT
);


-- =========================================
-- 6. SAFETY RULE
-- Prevent deleting a component while assigned
-- =========================================
CREATE OR REPLACE FUNCTION check_component_assignment()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.workstation_id IS NOT NULL THEN
        RAISE EXCEPTION
            'Cannot delete component % while it is assigned to a workstation. Move it to storage first.',
            OLD.asset_tag;
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restrict_component_delete
BEFORE DELETE ON component
FOR EACH ROW
EXECUTE FUNCTION check_component_assignment();


-- =========================================
-- 7. MAINTENANCE STATUS VIEW
-- =========================================
CREATE OR REPLACE VIEW view_maintenance_status AS
WITH last_logs AS (
    SELECT
        workstation_id,
        maintenance_type_id,
        MAX(performed_at) AS last_performed
    FROM maintenance_log
    GROUP BY workstation_id, maintenance_type_id
)
SELECT
    w.id AS workstation_id,
    w.name AS workstation_name,
    mt.id AS maintenance_type_id,
    mt.name AS maintenance_name,
    mt.interval_days,
    ll.last_performed,
    (ll.last_performed + (mt.interval_days * INTERVAL '1 day')) AS next_due_date,
    CASE
        WHEN ll.last_performed IS NULL THEN 'NEVER_DONE'
        WHEN (ll.last_performed + (mt.interval_days * INTERVAL '1 day')) < now() THEN 'OVERDUE'
        WHEN (ll.last_performed + (mt.interval_days * INTERVAL '1 day')) < (now() + INTERVAL '3 days') THEN 'DUE_SOON'
        ELSE 'OK'
    END AS status
FROM workstation w
CROSS JOIN maintenance_type mt
LEFT JOIN last_logs ll
    ON ll.workstation_id = w.id
   AND ll.maintenance_type_id = mt.id
WHERE mt.is_active = TRUE;


-- =========================================
-- 8. PERFORMANCE INDEXES
-- =========================================
CREATE INDEX idx_workstation_status_id
ON workstation(status_id);

CREATE INDEX idx_component_category_id
ON component(category_id);

CREATE INDEX idx_component_status_id
ON component(status_id);

CREATE INDEX idx_component_workstation_id
ON component(workstation_id);

CREATE INDEX idx_assignment_log_component_id
ON component_assignment_log(component_id);

CREATE INDEX idx_assignment_log_workstation_id
ON component_assignment_log(workstation_id);

CREATE INDEX idx_assignment_log_assigned_by_user_id
ON component_assignment_log(assigned_by_user_id);

CREATE INDEX idx_assignment_log_assigned_at
ON component_assignment_log(assigned_at);

CREATE INDEX idx_maintenance_log_workstation_id
ON maintenance_log(workstation_id);

CREATE INDEX idx_maintenance_log_type_id
ON maintenance_log(maintenance_type_id);

CREATE INDEX idx_maintenance_log_performed_by_user_id
ON maintenance_log(performed_by_user_id);

CREATE INDEX idx_maintenance_log_performed_at
ON maintenance_log(performed_at);