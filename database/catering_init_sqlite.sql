-- database: catering.db

-- 1) FIRST REMOVE ALL TABLES (in reverse dependency order)
DROP TABLE IF EXISTS Assignment;
DROP TABLE IF EXISTS Tasks;
DROP TABLE IF EXISTS SummarySheets;
DROP TABLE IF EXISTS ShiftBookings;
DROP TABLE IF EXISTS Shifts;
DROP TABLE IF EXISTS RecipePreparations;
DROP TABLE IF EXISTS UserRoles;
DROP TABLE IF EXISTS Services;
DROP TABLE IF EXISTS MenuItems;
DROP TABLE IF EXISTS MenuFeatures;
DROP TABLE IF EXISTS MenuSections;
DROP TABLE IF EXISTS Menus;
DROP TABLE IF EXISTS Events;
DROP TABLE IF EXISTS Preparations;
DROP TABLE IF EXISTS Recipes;
DROP TABLE IF EXISTS Roles;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Teams;
DROP TABLE IF EXISTS TeamMembers;
DROP TABLE IF EXISTS Employees;
DROP TABLE IF EXISTS Contracts;
DROP TABLE IF EXISTS Vacations;
DROP TABLE IF EXISTS StaffAssociations;
DROP TABLE IF EXISTS StaffLists;

-- 2) CREATE ALL TABLES (in dependency order)
CREATE TABLE Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL
);

CREATE TABLE Roles (
    id INTEGER PRIMARY KEY,
    role TEXT UNIQUE NOT NULL
);

CREATE TABLE UserRoles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES Roles (id) ON DELETE CASCADE
);

CREATE TABLE Recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE Preparations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE RecipePreparations (
    recipe_id INTEGER NOT NULL,
    preparation_id INTEGER NOT NULL,
    PRIMARY KEY (recipe_id, preparation_id),
    FOREIGN KEY (recipe_id) REFERENCES Recipes (id) ON DELETE CASCADE,
    FOREIGN KEY (preparation_id) REFERENCES Preparations (id) ON DELETE CASCADE
);

CREATE TABLE Events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    date_start INTEGER NOT NULL,
    date_end INTEGER,
    chef_id INTEGER NOT NULL,
    FOREIGN KEY (chef_id) REFERENCES Users (id)
);

CREATE TABLE Menus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    owner_id INTEGER NOT NULL,
    published INTEGER DEFAULT 0,
    FOREIGN KEY (owner_id) REFERENCES Users (id)
);

CREATE TABLE MenuSections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    position INTEGER NOT NULL,
    FOREIGN KEY (menu_id) REFERENCES Menus (id) ON DELETE CASCADE
);

CREATE TABLE MenuFeatures (
    menu_id INTEGER NOT NULL,
    feature TEXT NOT NULL,
    value INTEGER DEFAULT 0,
    PRIMARY KEY (menu_id, feature),
    FOREIGN KEY (menu_id) REFERENCES Menus (id) ON DELETE CASCADE
);

CREATE TABLE MenuItems (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_id INTEGER NOT NULL,
    section_id INTEGER DEFAULT 0,
    description TEXT NOT NULL,
    recipe_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    FOREIGN KEY (menu_id) REFERENCES Menus (id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES MenuSections (id) ON DELETE SET DEFAULT,
    FOREIGN KEY (recipe_id) REFERENCES Recipes (id)
);

CREATE TABLE Services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    approved_menu_id INTEGER DEFAULT 0,
    service_date INTEGER,
    time_start TEXT,
    time_end TEXT,
    location TEXT,
    FOREIGN KEY (event_id) REFERENCES Events (id) ON DELETE CASCADE,
    FOREIGN KEY (approved_menu_id) REFERENCES Menus (id) ON DELETE SET DEFAULT
);

CREATE TABLE Shifts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL
);

CREATE TABLE ShiftBookings (
    shift_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (shift_id, user_id),
    FOREIGN KEY (shift_id) REFERENCES Shifts (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE
);

CREATE TABLE SummarySheets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id INTEGER NOT NULL,
    owner_id INTEGER NOT NULL,
    FOREIGN KEY (service_id) REFERENCES Services (id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES Users (id)
);

CREATE TABLE Tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sumsheet_id INTEGER NOT NULL,
    kitchenproc_id INTEGER NOT NULL,
    description TEXT NOT NULL,
    type INTEGER NOT NULL,
    position INTEGER NOT NULL,
    ready INTEGER DEFAULT 0,
    quantity INTEGER DEFAULT 0,
    portions INTEGER DEFAULT 0,
    FOREIGN KEY (sumsheet_id) REFERENCES SummarySheets (id) ON DELETE CASCADE
);

CREATE TABLE Assignment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sumsheet_id INTEGER NOT NULL,
    shift_id INTEGER NOT NULL,
    task_id INTEGER NOT NULL,
    cook_id INTEGER DEFAULT 0,
    FOREIGN KEY (sumsheet_id) REFERENCES SummarySheets (id) ON DELETE CASCADE,
    FOREIGN KEY (shift_id) REFERENCES Shifts (id),
    FOREIGN KEY (task_id) REFERENCES Tasks (id) ON DELETE CASCADE,
    FOREIGN KEY (cook_id) REFERENCES Users (id) ON DELETE SET DEFAULT
);

CREATE TABLE Employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    fiscal_code TEXT UNIQUE NOT NULL,
    email TEXT,
    phone_number TEXT,
    vacation_days_left INTEGER DEFAULT 30
);

CREATE TABLE Contracts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    engagement TEXT NOT NULL CHECK (engagement IN ('permanente', 'occasionale')),
    start_date INTEGER NOT NULL,
    end_date INTEGER,
    employee_id INTEGER NOT NULL,
    created_date INTEGER DEFAULT (strftime('%s', 'now')),
    FOREIGN KEY (employee_id) REFERENCES Employees (id) ON DELETE CASCADE
);

CREATE TABLE Vacations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    start_date INTEGER NOT NULL,
    end_date INTEGER NOT NULL,
    state TEXT NOT NULL DEFAULT 'Non valutata',
    requested_date INTEGER DEFAULT (strftime('%s', 'now')),
    evaluated_date INTEGER,
    FOREIGN KEY (employee_id) REFERENCES Employees (id) ON DELETE CASCADE
);

CREATE TABLE Teams (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    staff_to_assign INTEGER DEFAULT 0,
    created_date INTEGER DEFAULT (strftime('%s', 'now'))
);

CREATE TABLE TeamMembers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    engagement TEXT NOT NULL CHECK (engagement IN ('permanente', 'occasionale')),
    FOREIGN KEY (team_id) REFERENCES Teams (id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES Employees (id) ON DELETE CASCADE,
    UNIQUE (team_id, employee_id)
);

CREATE TABLE StaffLists (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    owner_id INTEGER NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES Users (id)
);

CREATE TABLE StaffAssociations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stafflist_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    engagement TEXT NOT NULL CHECK (engagement IN ('permanente', 'occasionale')),
    FOREIGN KEY (stafflist_id) REFERENCES StaffLists (id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES Employees (id) ON DELETE CASCADE,
    UNIQUE (stafflist_id, employee_id)
);

-- Insert data
INSERT INTO Roles (id, role) VALUES
(0, 'CUOCO'),
(1, 'CHEF'),
(2, 'ORGANIZZATORE'),
(3, 'SERVIZIO'),
(4, 'PROPRIETARIO');

INSERT INTO Preparations (name, description) VALUES
('Soffritto', 'Base aromatica con cipolla, carota e sedano'),
('Brodo vegetale', 'Brodo preparato con verdure fresche'),
('Brodo di carne', 'Brodo preparato con ossa e carne di manzo'),
('Salsa di pomodoro', 'Salsa base di pomodoro con basilico'),
('Ragù alla bolognese', 'Sugo di carne tradizionale emiliano'),
('Besciamella', 'Salsa bianca a base di burro, farina e latte'),
('Salsa carbonara', 'Salsa con uova, pecorino e guanciale'),
('Impasto per pasta', 'Impasto base per pasta fresca'),
('Marinatura per carne', 'Marinatura con erbe e spezie per carni'),
('Marinatura per pesce', 'Marinatura con limone ed erbe per pesce'),
('Crema al mascarpone', 'Crema dolce a base di mascarpone'),
('Crema pasticcera', 'Crema per dolci a base di uova e latte'),
('Pasta frolla', 'Impasto base per crostate'),
('Impasto per pizza', 'Impasto lievitato per pizza');

INSERT INTO Recipes (name, description) VALUES
-- ANTIPASTI
('Bruschetta al Pomodoro', 'Pane tostato con pomodori freschi e basilico'),
('Caprese', 'Mozzarella di bufala con pomodori e basilico'),
('Antipasto Misto', 'Selezione di salumi e formaggi locali'),
('Arancini di Riso', 'Sfere di riso ripiene fritte'),
('Caponata Siciliana', 'Verdure in agrodolce della tradizione siciliana'),
('Prosciutto e Melone', 'Prosciutto crudo con melone cantaloupe'),
-- PRIMI PIATTI
('Spaghetti alla Carbonara', 'Pasta con uova, pecorino, guanciale e pepe'),
('Risotto ai Funghi Porcini', 'Risotto cremoso con funghi porcini'),
('Lasagne alla Bolognese', 'Lasagne con ragù, besciamella e parmigiano'),
('Tagliatelle al Ragù', 'Pasta fresca con ragù alla bolognese'),
('Pasta alla Norma', 'Pasta con melanzane, pomodoro e ricotta salata'),
('Ossobuco alla Milanese', 'Stinco di vitello brasato con gremolata'),
-- SECONDI PIATTI
('Saltimbocca alla Romana', 'Scaloppine di vitello con prosciutto e salvia'),
('Pesce al Forno con Patate', 'Pesce fresco al forno con patate'),
('Pollo alla Cacciatora', 'Pollo in umido con verdure'),
('Vitello Tonnato', 'Vitello freddo con salsa tonnata'),
('Melanzane alla Parmigiana', 'Melanzane gratinate con pomodoro e mozzarella'),
-- DOLCI
('Tiramisù', 'Dolce al cucchiaio con caffè e mascarpone'),
('Panna Cotta', 'Dolce al cucchiaio alla vaniglia'),
('Cannoli Siciliani', 'Cannoli croccanti con ricotta dolce'),
('Gelato Artigianale', 'Gelato fatto in casa con ingredienti naturali'),
('Crostata di Frutta', 'Crostata con crema e frutta di stagione'),
-- PIZZE
('Pizza Margherita', 'Pizza con pomodoro, mozzarella e basilico'),
('Pizza Quattro Formaggi', 'Pizza con quattro tipi di formaggio'),
('Pizza Diavola', 'Pizza piccante con salame piccante'),
('Pizza Marinara', 'Pizza con pomodoro, aglio e origano'),
-- ALTRI PIATTI
('Polenta con Funghi', 'Polenta cremosa con funghi misti'),
('Ribollita Toscana', 'Zuppa di pane e verdure toscana'),
('Canederli Tirolesi', 'Gnocchi di pane in brodo'),
('Risotto alla Milanese', 'Risotto con zafferano mantecato');

-- Associate recipes with preparations
INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Bruschetta al Pomodoro' AND p.name = 'Salsa di pomodoro';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Arancini di Riso' AND p.name = 'Ragù alla bolognese';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Caponata Siciliana' AND p.name = 'Soffritto';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Spaghetti alla Carbonara' AND p.name = 'Salsa carbonara';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Risotto ai Funghi Porcini' AND p.name = 'Brodo vegetale';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Lasagne alla Bolognese' AND p.name = 'Ragù alla bolognese';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Lasagne alla Bolognese' AND p.name = 'Besciamella';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Lasagne alla Bolognese' AND p.name = 'Impasto per pasta';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Tagliatelle al Ragù' AND p.name = 'Ragù alla bolognese';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Tagliatelle al Ragù' AND p.name = 'Impasto per pasta';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pasta alla Norma' AND p.name = 'Salsa di pomodoro';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Melanzane alla Parmigiana' AND p.name = 'Salsa di pomodoro';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pesce al Forno con Patate' AND p.name = 'Marinatura per pesce';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Vitello Tonnato' AND p.name = 'Marinatura per carne';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Tiramisù' AND p.name = 'Crema al mascarpone';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Cannoli Siciliani' AND p.name = 'Crema pasticcera';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Crostata di Frutta' AND p.name = 'Pasta frolla';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Crostata di Frutta' AND p.name = 'Crema pasticcera';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pizza Margherita' AND p.name = 'Impasto per pizza';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pizza Margherita' AND p.name = 'Salsa di pomodoro';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pizza Quattro Formaggi' AND p.name = 'Impasto per pizza';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pizza Diavola' AND p.name = 'Impasto per pizza';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pizza Diavola' AND p.name = 'Salsa di pomodoro';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pizza Marinara' AND p.name = 'Impasto per pizza';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Pizza Marinara' AND p.name = 'Salsa di pomodoro';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Polenta con Funghi' AND p.name = 'Brodo vegetale';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Ribollita Toscana' AND p.name = 'Soffritto';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Canederli Tirolesi' AND p.name = 'Brodo di carne';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p WHERE r.name = 'Risotto alla Milanese' AND p.name = 'Brodo vegetale';

-- Insert users with proper roles
INSERT INTO Users (username) VALUES 
('Marco'), ('Giulia'), ('Luca'), ('Sofia'), ('Antonio'), 
('Chiara'), ('Giovanni'), ('Francesca'), ('Mario Rossi'), ('Luigi Verdi');

-- Assign roles to users
INSERT INTO UserRoles (user_id, role_id) VALUES
(1, 3),  -- Marco: SERVIZIO
(2, 3),  -- Giulia: SERVIZIO
(3, 0),  -- Luca: CUOCO
(4, 0),  -- Sofia: CUOCO
(5, 1),  -- Antonio: CHEF
(6, 1),  -- Chiara: CHEF
(7, 2),  -- Giovanni: ORGANIZZATORE
(8, 2),  -- Francesca: ORGANIZZATORE
(9, 4),  -- Mario Rossi: PROPRIETARIO
(10, 2); -- Luigi Verdi: ORGANIZZATORE

-- Create sample menu
INSERT INTO Menus (title, owner_id, published) VALUES ('Sample Menu', 5, 1);

INSERT INTO MenuSections (menu_id, name, position) VALUES 
(1, 'Appetizers', 0),
(1, 'Main Courses', 1),
(1, 'Desserts', 2);

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 1, 'Caprese', r.id, 0 FROM Recipes r WHERE r.name = 'Caprese';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 1, 'Prosciutto e Melone', r.id, 1 FROM Recipes r WHERE r.name = 'Prosciutto e Melone';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 2, 'Spaghetti alla Carbonara', r.id, 0 FROM Recipes r WHERE r.name = 'Spaghetti alla Carbonara';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 2, 'Ossobuco alla Milanese', r.id, 1 FROM Recipes r WHERE r.name = 'Ossobuco alla Milanese';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 3, 'Panna Cotta', r.id, 0 FROM Recipes r WHERE r.name = 'Panna Cotta';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 3, 'Gelato Artigianale', r.id, 1 FROM Recipes r WHERE r.name = 'Gelato Artigianale';

-- Create sample event and services
INSERT INTO Events (name, date_start, date_end, chef_id) VALUES 
('Gala Aziendale Annuale', date('2025-06-15'), date('2025-06-16'), 5);

INSERT INTO Services (event_id, name, approved_menu_id, service_date, time_start, time_end, location) VALUES 
(1, 'Pranzo Buffet Aziendale', 1, date('2025-06-15'), time('12:00'), time('15:00'), 'Salone Grande');

INSERT INTO Services (event_id, name, approved_menu_id, service_date, time_start, time_end, location) VALUES 
(1, 'Cena di Gala', 0, date('2025-06-15'), time('19:00'), time('23:00'), 'Sala Esecutiva');

-- Insert sample employees
INSERT INTO Employees (name, fiscal_code, email, phone_number, vacation_days_left) VALUES 
('Goran Pandev', 'GRNPND80A01H501U', 'goran.pandev@catering.com', '+39 320 1234567', 25),
('Claudio Lupo', 'CLDLPO85B02H501V', 'claudio.lupo@catering.com', '+39 320 2345678', 30),
('Dario Mangini', 'DARMNG90C03H501W', 'dario.mangini@catering.com', '+39 320 3456789', 20),
('Raffaele Melluso', 'RFFMLL92D04H501X', 'raffaele.melluso@catering.com', '+39 320 4567890', 28);