-- 1. USERS & ROLES
CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER', 'OWNER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. WORKSTATION MANAGEMENT
CREATE TABLE workstation_status (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE workstation (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    status_id INT REFERENCES workstation_status(id),
    grid_x INT DEFAULT 0,
    grid_y INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. COMPONENT INVENTORY
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
    serial_number VARCHAR(100) UNIQUE,
    name VARCHAR(200) NOT NULL,
    category_id INT NOT NULL REFERENCES component_category(id),
    status_id INT NOT NULL REFERENCES component_status(id),
    
    -- When a workstation is deleted, the part goes back to 'Storage' (NULL)
    workstation_id INT REFERENCES workstation(id) ON DELETE SET NULL, 
    
    purchase_date DATE,
    warranty_expiry DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. MAINTENANCE SYSTEM
CREATE TABLE maintenance_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    interval_days INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE maintenance_log (
    id SERIAL PRIMARY KEY,
    workstation_id INT NOT NULL REFERENCES workstation(id) ON DELETE CASCADE,
    -- Fixed the reference name here:
    maintenance_type_id INT NOT NULL REFERENCES maintenance_type(id),
    
    performed_by_user_id INT REFERENCES app_user(id),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- 5. SAFETY TRIGGER: Prevent component deletion if assigned to a workstation
-- This enforces your rule: "restricted if it belongs to the workstation"
CREATE OR REPLACE FUNCTION check_component_assignment()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.workstation_id IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot delete component % while it is assigned to a workstation. Move it to storage first.', OLD.serial_number;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restrict_component_delete
BEFORE DELETE ON component
FOR EACH ROW EXECUTE FUNCTION check_component_assignment();

CREATE OR REPLACE VIEW view_maintenance_status AS
WITH last_logs AS (
    -- 1. Get the most recent log for every workstation + maintenance type combo
    SELECT 
        workstation_id,
        maintenance_type_id,
        MAX(performed_at) as last_performed
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
    
    -- 2. Calculate Next Due Date
    -- If never performed, next_due is NULL (or could be set to created_at + interval)
    (ll.last_performed + (mt.interval_days * INTERVAL '1 day')) AS next_due_date,

    -- 3. Compute Status based on requirements
    CASE 
        WHEN ll.last_performed IS NULL THEN 'NEVER_DONE'
        WHEN (ll.last_performed + (mt.interval_days * INTERVAL '1 day')) < CURRENT_TIMESTAMP THEN 'OVERDUE'
        WHEN (ll.last_performed + (mt.interval_days * INTERVAL '1 day')) < (CURRENT_TIMESTAMP + INTERVAL '3 days') THEN 'DUE_SOON'
        ELSE 'OK'
    END AS status

FROM workstation w
CROSS JOIN maintenance_type mt -- 4. Cross Join ensures we check every active type for every workstation
LEFT JOIN last_logs ll ON w.id = ll.workstation_id AND mt.id = ll.maintenance_type_id
WHERE mt.is_active = TRUE;





-- INSERTS====================================================


-- Users (Password is 'password123' hashed with bcrypt placeholder)
INSERT INTO app_user (email, password_hash, full_name, role) VALUES
('owner@trackrig.com', '$2a$12$R9h/cIPz0gi.URNNXRfx.O8fQ8U', 'Alice Owner', 'OWNER'),
('manager@trackrig.com', '$2a$12$R9h/cIPz0gi.URNNXRfx.O8fQ8U', 'Bob Manager', 'MANAGER'),
('tech@trackrig.com', '$2a$12$R9h/cIPz0gi.URNNXRfx.O8fQ8U', 'Charlie Tech', 'EMPLOYEE');

-- Workstation Statuses
INSERT INTO workstation_status (name) VALUES 
('Operational'), 
('Under Maintenance'), 
('Out of Order');

-- Component Categories (Dynamic types)
INSERT INTO component_category (name, description) VALUES 
('GPU', 'Graphics Processing Units'),
('CPU', 'Central Processing Units'),
('RAM', 'System Memory Modules'),
('Motherboard', 'Main Logic Boards'),
('Peripheral', 'Keyboards, Mice, Headsets');

-- Component Statuses
INSERT INTO component_status (name) VALUES 
('Working'), 
('Damaged'), 
('RMA Pending');

-- Maintenance Types (Rules for intervals)
INSERT INTO maintenance_type (name, interval_days, description) VALUES 
('Dust Cleaning', 30, 'Clean dust filters and fans with compressed air'),
('Thermal Repaste', 180, 'Replace CPU/GPU thermal compound'),
('OS Update & Driver Check', 14, 'Windows updates and NVIDIA driver installation');

-- Generating a grid layout (Rows of 5 PCs)
INSERT INTO workstation (name, status_id, grid_x, grid_y) VALUES
('Station-A1', 1, 0, 0), ('Station-A2', 1, 1, 0), ('Station-A3', 1, 2, 0), ('Station-A4', 1, 3, 0), ('Station-A5', 2, 4, 0),
('Station-B1', 1, 0, 1), ('Station-B2', 1, 1, 1), ('Station-B3', 3, 2, 1), ('Station-B4', 1, 3, 1), ('Station-B5', 1, 4, 1),
('Station-C1', 1, 0, 2), ('Station-C2', 1, 1, 2), ('Station-C3', 1, 2, 2), ('Station-C4', 2, 3, 2), ('Station-C5', 1, 4, 2);

INSERT INTO component (serial_number, name, category_id, status_id, workstation_id, purchase_date) VALUES
-- GPUs Installed in Stations A1-A5
('SN-GPU-001', 'NVIDIA RTX 4080', 1, 1, 1, '2023-01-15'),
('SN-GPU-002', 'NVIDIA RTX 4080', 1, 1, 2, '2023-01-15'),
('SN-GPU-003', 'NVIDIA RTX 4070', 1, 1, 3, '2023-02-20'),
('SN-GPU-004', 'NVIDIA RTX 4070', 1, 1, 4, '2023-02-20'),
('SN-GPU-005', 'NVIDIA RTX 3090', 1, 2, 5, '2022-11-10'), -- Damaged GPU in Station A5

-- CPUs Installed in Stations B1-B3
('SN-CPU-101', 'Intel Core i9-13900K', 2, 1, 6, '2023-01-15'),
('SN-CPU-102', 'Intel Core i9-13900K', 2, 1, 7, '2023-01-15'),
('SN-CPU-103', 'AMD Ryzen 9 7950X', 2, 1, 8, '2023-03-05'),

-- Peripherals Installed
('SN-KB-201', 'Razer BlackWidow V4', 5, 1, 1, '2023-06-01'),
('SN-MS-202', 'Logitech G Pro X', 5, 1, 1, '2023-06-01'),

-- SPARE PARTS (In Storage / No Workstation ID)
('SN-GPU-999', 'NVIDIA RTX 3060 (Spare)', 1, 1, NULL, '2022-08-15'),
('SN-RAM-888', 'Corsair Vengeance 32GB Kit', 3, 1, NULL, '2023-04-10'),
('SN-PSU-777', 'Seasonic 850W Gold', 5, 1, NULL, '2023-05-20'),
('SN-KB-666', 'Keychron K2 (Backup)', 5, 1, NULL, '2022-12-01'),
('SN-MB-555', 'ASUS ROG Strix Z790', 4, 3, NULL, '2023-01-10'); -- RMA Pending Motherboard

INSERT INTO maintenance_log (workstation_id, maintenance_type_id, performed_by_user_id, performed_at, notes) VALUES
-- Dust Cleaning (Interval: 30 days)
(1, 1, 3, CURRENT_TIMESTAMP - INTERVAL '5 days', 'Cleaned filters, looks good'),   -- STATUS: OK
(2, 1, 3, CURRENT_TIMESTAMP - INTERVAL '28 days', 'Lots of dust buildup'),       -- STATUS: DUE_SOON
(3, 1, 3, CURRENT_TIMESTAMP - INTERVAL '45 days', 'Fans were clogged'),          -- STATUS: OVERDUE (30 day interval exceeded)
(4, 1, 3, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Routine clean'),               -- STATUS: OK
(5, 1, 3, CURRENT_TIMESTAMP - INTERVAL '60 days', 'Missed last cycle'),          -- STATUS: OVERDUE

-- OS Updates (Interval: 14 days)
(1, 3, 2, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Updated drivers to v536.23'),   -- STATUS: OK
(2, 3, 2, CURRENT_TIMESTAMP - INTERVAL '13 days', 'Windows update pending'),     -- STATUS: DUE_SOON
(3, 3, 2, CURRENT_TIMESTAMP - INTERVAL '20 days', 'Drivers outdated'),           -- STATUS: OVERDUE

-- Thermal Repaste (Interval: 180 days)
(6, 2, 3, CURRENT_TIMESTAMP - INTERVAL '100 days', 'Temps stable at 65C'),       -- STATUS: OK
(7, 2, 3, CURRENT_TIMESTAMP - INTERVAL '190 days', 'Running hot, needs redo'),   -- STATUS: OVERDUE
(8, 2, 3, CURRENT_TIMESTAMP - INTERVAL '178 days', 'Temps climbing slightly'),   -- STATUS: DUE_SOON

-- Random Maintenance on other stations
(9, 1, 3, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Clean'),
(10, 1, 3, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Clean'),
(11, 1, 3, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Clean'),
(12, 1, 3, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Clean');


SELECT workstation_name, maintenance_name, status, next_due_date 
FROM view_maintenance_status 
WHERE status != 'OK'
ORDER BY next_due_date ASC;

-- 1. Ispravka za komponente
-- Postavljamo serial_number na NOT NULL (pod pretpostavkom da trenutno nemaš NULL vrijednosti u bazi)
ALTER TABLE component 
    ALTER COLUMN serial_number SET NOT NULL;

-- 2. Ispravka za radne stanice
-- Status radne stanice ne bi smio biti nepoznat
ALTER TABLE workstation 
    ALTER COLUMN status_id SET NOT NULL;

-- 3. Ispravka za tipove održavanja
-- Osiguravamo da ne postoje dva tipa sa istim nazivom (npr. "Čišćenje prašine")
ALTER TABLE maintenance_type 
    ADD CONSTRAINT uq_maintenance_type_name UNIQUE (name);

-- 4. Osiguravanje jedinstvenih pozicija (koordinata)
ALTER TABLE workstation
    ADD CONSTRAINT uq_workstation_grid UNIQUE (grid_x, grid_y);



