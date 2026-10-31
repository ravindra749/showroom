CREATE TABLE vehicle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vin VARCHAR(17) NOT NULL,
    make VARCHAR(60) NOT NULL,
    model VARCHAR(60) NOT NULL,
    model_year INT NOT NULL,
    trim_level VARCHAR(60),
    body_type VARCHAR(20) NOT NULL,
    color VARCHAR(40) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    mileage INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_vehicle_vin UNIQUE (vin)
);

CREATE TABLE test_drive (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    preferred_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_test_drive_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id)
);
