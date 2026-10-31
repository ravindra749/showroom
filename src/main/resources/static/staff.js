"use strict";

const vehicleForm = document.getElementById("vehicleForm");
const vehicleFormHeading = document.getElementById("vehicleFormHeading");
const vehicleFormMessage = document.getElementById("vehicleFormMessage");
const saveVehicleButton = document.getElementById("saveVehicle");
const cancelVehicleEditButton = document.getElementById("cancelVehicleEdit");
const bodyTypeSelect = document.getElementById("bodyType");
const refreshInventoryButton = document.getElementById("refreshInventory");
const inventoryMessage = document.getElementById("inventoryMessage");
const inventoryTableBody = document.getElementById("inventoryTableBody");
const testDriveStatusFilter = document.getElementById("testDriveStatusFilter");
const testDriveMessage = document.getElementById("testDriveMessage");
const testDriveTableBody = document.getElementById("testDriveTableBody");

// Buttons offered for each booking status, matching TestDriveStatus.canChangeTo.
const NEXT_STEPS = {
    REQUESTED: [["CONFIRMED", "Confirm"], ["CANCELLED", "Cancel"]],
    CONFIRMED: [["COMPLETED", "Mark completed"], ["CANCELLED", "Cancel"]],
    COMPLETED: [],
    CANCELLED: [],
};

let vehicles = [];
let editingVehicleId = null;

function field(id) {
    return document.getElementById(id);
}

function readVehicleForm() {
    return {
        vin: field("vin").value.trim(),
        make: field("make").value.trim(),
        model: field("model").value.trim(),
        modelYear: Number(field("modelYear").value),
        trimLevel: field("trimLevel").value.trim() || null,
        bodyType: field("bodyType").value,
        color: field("color").value.trim(),
        price: Number(field("price").value),
        mileage: Number(field("mileage").value),
        status: field("vehicleStatus").value,
    };
}

function resetVehicleForm() {
    editingVehicleId = null;
    vehicleForm.reset();
    vehicleFormHeading.textContent = "Add a vehicle";
    saveVehicleButton.textContent = "Add vehicle";
    cancelVehicleEditButton.hidden = true;
}

function startEditing(vehicle) {
    editingVehicleId = vehicle.id;
    field("vin").value = vehicle.vin;
    field("make").value = vehicle.make;
    field("model").value = vehicle.model;
    field("modelYear").value = vehicle.modelYear;
    field("trimLevel").value = vehicle.trimLevel ?? "";
    field("bodyType").value = vehicle.bodyType;
    field("color").value = vehicle.color;
    field("price").value = vehicle.price;
    field("mileage").value = vehicle.mileage;
    field("vehicleStatus").value = vehicle.status;
    vehicleFormHeading.textContent = `Edit ${vehicleName(vehicle)}`;
    saveVehicleButton.textContent = "Save changes";
    cancelVehicleEditButton.hidden = false;
    setStatus(vehicleFormMessage, "");
    vehicleForm.scrollIntoView({ behavior: "smooth", block: "start" });
    field("make").focus();
}

function addCell(row, value) {
    const cell = document.createElement("td");
    if (value instanceof Node) {
        cell.append(value);
    } else {
        cell.textContent = value ?? "";
    }
    row.append(cell);
    return cell;
}

function actionButton(label, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "button-secondary button-small";
    button.textContent = label;
    button.addEventListener("click", onClick);
    return button;
}

function renderInventory() {
    inventoryTableBody.replaceChildren();
    for (const vehicle of vehicles) {
        const row = document.createElement("tr");
        addCell(row, vehicleName(vehicle));
        addCell(row, vehicle.vin);
        addCell(row, `${BODY_TYPE_LABELS[vehicle.bodyType] ?? vehicle.bodyType} / ${vehicle.color}`);
        addCell(row, priceFormat.format(vehicle.price));
        addCell(row, numberFormat.format(vehicle.mileage));
        addCell(row, makeBadge(vehicle.status));

        const actions = document.createElement("div");
        actions.className = "actions";
        actions.append(
            actionButton("Edit", () => startEditing(vehicle)),
            actionButton("Delete", () => deleteVehicle(vehicle)),
        );
        addCell(row, actions);
        inventoryTableBody.append(row);
    }
}

async function loadInventory() {
    refreshInventoryButton.disabled = true;
    setStatus(inventoryMessage, "Loading inventory…");
    try {
        const response = await fetch("/api/vehicles");
        if (!response.ok) {
            throw new Error(await readError(response));
        }
        vehicles = await response.json();
        renderInventory();
        setStatus(inventoryMessage, vehicles.length === 0
            ? "No vehicles yet. Add the first one above."
            : `${vehicles.length} vehicle${vehicles.length === 1 ? "" : "s"} in inventory.`);
    } catch (error) {
        setStatus(inventoryMessage, `Could not load inventory: ${error.message}`, "error");
    } finally {
        refreshInventoryButton.disabled = false;
    }
}

async function deleteVehicle(vehicle) {
    if (!window.confirm(`Delete ${vehicleName(vehicle)} (VIN ${vehicle.vin})?`)) {
        return;
    }
    try {
        const response = await fetch(`/api/vehicles/${vehicle.id}`, { method: "DELETE" });
        if (!response.ok) {
            throw new Error(await readError(response));
        }
        if (editingVehicleId === vehicle.id) {
            resetVehicleForm();
        }
        setStatus(inventoryMessage, `Deleted ${vehicleName(vehicle)}.`, "success");
        await loadInventory();
    } catch (error) {
        setStatus(inventoryMessage, `Could not delete the vehicle: ${error.message}`, "error");
    }
}

vehicleForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const isEditing = editingVehicleId !== null;
    saveVehicleButton.disabled = true;
    setStatus(vehicleFormMessage, "Saving…");
    try {
        const response = await fetch(
            isEditing ? `/api/vehicles/${editingVehicleId}` : "/api/vehicles",
            {
                method: isEditing ? "PUT" : "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(readVehicleForm()),
            },
        );
        if (!response.ok) {
            throw new Error(await readError(response));
        }
        const saved = await response.json();
        resetVehicleForm();
        setStatus(vehicleFormMessage,
            `${isEditing ? "Updated" : "Added"} ${vehicleName(saved)}.`, "success");
        await loadInventory();
    } catch (error) {
        setStatus(vehicleFormMessage, `Could not save the vehicle: ${error.message}`, "error");
    } finally {
        saveVehicleButton.disabled = false;
    }
});

cancelVehicleEditButton.addEventListener("click", () => {
    resetVehicleForm();
    setStatus(vehicleFormMessage, "");
});

function renderTestDrives(testDrives) {
    testDriveTableBody.replaceChildren();
    for (const testDrive of testDrives) {
        const row = document.createElement("tr");
        addCell(row, testDrive.id);
        addCell(row, testDrive.vehicleName);

        const customer = document.createElement("div");
        const name = document.createElement("strong");
        name.textContent = testDrive.customerName;
        const contact = document.createElement("span");
        contact.className = "small";
        contact.textContent = `${testDrive.email} · ${testDrive.phone}`;
        customer.append(name, contact);
        addCell(row, customer);

        addCell(row, testDrive.preferredDate);
        addCell(row, makeBadge(testDrive.status));

        const actions = document.createElement("div");
        actions.className = "actions";
        for (const [nextStatus, label] of NEXT_STEPS[testDrive.status] ?? []) {
            actions.append(actionButton(label, () => changeTestDriveStatus(testDrive, nextStatus)));
        }
        addCell(row, actions);
        testDriveTableBody.append(row);
    }
}

async function loadTestDrives() {
    setStatus(testDriveMessage, "Loading bookings…");
    const status = testDriveStatusFilter.value;
    try {
        const response = await fetch(
            status ? `/api/test-drives?status=${encodeURIComponent(status)}` : "/api/test-drives");
        if (!response.ok) {
            throw new Error(await readError(response));
        }
        const testDrives = await response.json();
        renderTestDrives(testDrives);
        setStatus(testDriveMessage, testDrives.length === 0
            ? "No bookings to show."
            : `${testDrives.length} booking${testDrives.length === 1 ? "" : "s"}.`);
    } catch (error) {
        setStatus(testDriveMessage, `Could not load bookings: ${error.message}`, "error");
    }
}

async function changeTestDriveStatus(testDrive, nextStatus) {
    if (nextStatus === "CANCELLED"
        && !window.confirm(`Cancel booking #${testDrive.id} for ${testDrive.customerName}?`)) {
        return;
    }
    try {
        const response = await fetch(`/api/test-drives/${testDrive.id}/status`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status: nextStatus }),
        });
        if (!response.ok) {
            throw new Error(await readError(response));
        }
        await loadTestDrives();
        setStatus(testDriveMessage,
            `Booking #${testDrive.id} is now ${titleCase(nextStatus).toLowerCase()}.`, "success");
    } catch (error) {
        setStatus(testDriveMessage, `Could not update booking: ${error.message}`, "error");
    }
}

refreshInventoryButton.addEventListener("click", loadInventory);
testDriveStatusFilter.addEventListener("change", loadTestDrives);

addOptions(bodyTypeSelect, Object.entries(BODY_TYPE_LABELS));
loadInventory();
loadTestDrives();
