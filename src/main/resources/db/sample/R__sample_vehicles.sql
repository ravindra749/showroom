-- Optional demo inventory, loaded only when FLYWAY_LOCATIONS includes
-- classpath:db/sample. INSERT IGNORE skips vehicles whose VIN already exists.
INSERT IGNORE INTO vehicle (vin, make, model, model_year, trim_level, body_type, color, price, mileage, status) VALUES
    ('1NLMS23A7RK100001', 'Northline', 'Aster', 2025, 'LE', 'SEDAN', 'Pearl White', 27990.00, 12, 'AVAILABLE'),
    ('1NLMS23A7RK100002', 'Northline', 'Aster', 2025, 'XSE Hybrid', 'SEDAN', 'Midnight Blue', 34450.00, 8, 'AVAILABLE'),
    ('1NLMT45C2RK100003', 'Northline', 'Ridgeway', 2025, 'Adventure AWD', 'SUV', 'Forest Green', 41200.00, 15, 'AVAILABLE'),
    ('1NLMT45C2RK100004', 'Northline', 'Ridgeway', 2024, 'Limited', 'SUV', 'Graphite', 38750.00, 14210, 'AVAILABLE'),
    ('1NLMP67D5RK100005', 'Northline', 'Haulmark', 2025, 'Crew Cab 4x4', 'TRUCK', 'Ruby Red', 52990.00, 5, 'RESERVED'),
    ('2KVHB12E9PK100006', 'Kestrel', 'Swift', 2023, 'Sport', 'HATCHBACK', 'Sunburst Orange', 19800.00, 22740, 'AVAILABLE'),
    ('2KVHC34F1PK100007', 'Kestrel', 'Vista', 2024, 'Touring', 'VAN', 'Silver', 36500.00, 9100, 'AVAILABLE'),
    ('3SLCC56G3RK100008', 'Solano', 'Breeze', 2025, 'GT', 'CONVERTIBLE', 'Arctic White', 46900.00, 3, 'SOLD');
