BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "appointment" (
	"appointment_id"	INTEGER,
	"owner_id"	INTEGER NOT NULL,
	"vehicle_id"	INTEGER NOT NULL,
	"service_type_id"	INTEGER,
	"date_time"	TEXT NOT NULL,
	"location"	TEXT,
	"notes"	TEXT,
	"status"	TEXT NOT NULL DEFAULT 'Booked',
	PRIMARY KEY("appointment_id" AUTOINCREMENT),
	FOREIGN KEY("owner_id") REFERENCES "car_owner"("owner_id"),
	FOREIGN KEY("service_type_id") REFERENCES "service_type"("service_type_id"),
	FOREIGN KEY("vehicle_id") REFERENCES "vehicle"("vehicle_id")
);
CREATE TABLE IF NOT EXISTS "car_owner" (
	"owner_id"	INTEGER,
	"full_name"	TEXT NOT NULL,
	"email"	TEXT NOT NULL UNIQUE,
	"phone"	TEXT,
	PRIMARY KEY("owner_id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "payment" (
	"payment_id"	INTEGER,
	"appointment_id"	INTEGER NOT NULL UNIQUE,
	"amount"	EURO NOT NULL,
	"method"	TEXT NOT NULL,
	"payment_status"	TEXT NOT NULL,
	"gateway_ref"	TEXT,
	"paid_at"	TEXT,
	PRIMARY KEY("payment_id" AUTOINCREMENT),
	FOREIGN KEY("appointment_id") REFERENCES "appointment"("appointment_id")
);
CREATE TABLE IF NOT EXISTS "service_type" (
	"service_type_id"	INTEGER,
	"name"	TEXT NOT NULL UNIQUE,
	"base_price"	REAL NOT NULL,
	"est_duration_mins"	INTEGER NOT NULL,
	PRIMARY KEY("service_type_id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "vehicle" (
	"vehicle_id"	INTEGER,
	"owner_id"	INTEGER NOT NULL,
	"make"	TEXT NOT NULL,
	"model"	TEXT NOT NULL,
	"year"	INTEGER NOT NULL,
	"reg_number"	TEXT NOT NULL UNIQUE,
	"mileage"	INTEGER DEFAULT 0,
	PRIMARY KEY("vehicle_id" AUTOINCREMENT),
	FOREIGN KEY("owner_id") REFERENCES "car_owner"("owner_id")
);
INSERT INTO "appointment" VALUES (1,1,1,1,'2026-04-17 10:00','Main Garage','','Booked');
INSERT INTO "appointment" VALUES (2,2,3,4,'2026-04-20 14:35','Main Garage','','Booked');
INSERT INTO "appointment" VALUES (3,3,2,2,'2026-06-29 16:00','Main Garage','','Cancelled');
INSERT INTO "appointment" VALUES (4,1,3,3,'2026-05-30 12:00','Main Garage','','Completed');
INSERT INTO "car_owner" VALUES (1,'Lucas Oliveira','lucas@email.com','0850000000');
INSERT INTO "car_owner" VALUES (2,'Maria Beatriz
','ana@email.com','0860000000');
INSERT INTO "car_owner" VALUES (3,'Mark Doyle','mark@email.com','0870000000');
INSERT INTO "payment" VALUES (5,1,0,'Card','Pending',NULL,NULL);
INSERT INTO "payment" VALUES (6,2,0,'Cash','Pending',NULL,NULL);
INSERT INTO "payment" VALUES (7,3,0,'Card','Pending',NULL,NULL);
INSERT INTO "payment" VALUES (8,4,0,'Cash','Pending',NULL,NULL);
INSERT INTO "service_type" VALUES (1,'Oil Change',79.99,45);
INSERT INTO "service_type" VALUES (2,'Tyre Replacement',120.0,60);
INSERT INTO "service_type" VALUES (3,'Diagnostics',60.0,30);
INSERT INTO "service_type" VALUES (4,'General Service',150.0,90);
INSERT INTO "vehicle" VALUES (1,1,'Opel','Insignia',2014,'14-D-12345',175000);
INSERT INTO "vehicle" VALUES (2,2,'Toyota','Corolla',2018,'18-D-54321',82000);
INSERT INTO "vehicle" VALUES (3,3,'Volkswagen','Golf',2016,'16-D-67890',130500);
COMMIT;
