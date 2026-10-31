"use strict";

const searchForm = document.getElementById("searchForm");
const makeFilter = document.getElementById("makeFilter");
const bodyTypeFilter = document.getElementById("bodyTypeFilter");
const maxPriceFilter = document.getElementById("maxPriceFilter");
const resetSearchButton = document.getElementById("resetSearch");
const resultsMessage = document.getElementById("resultsMessage");
const vehicleGrid = document.getElementById("vehicleGrid");
const bookingPanel = document.getElementById("bookingPanel");
const bookingVehicle = document.getElementById("bookingVehicle");
const bookingForm = document.getElementById("bookingForm");
const bookingVehicleId = document.getElementById("bookingVehicleId");
const customerName = document.getElementById("customerName");
const customerEmail = document.getElementById("customerEmail");
const customerPhone = document.getElementById("customerPhone");
const preferredDate = document.getElementById("preferredDate");
const submitBookingButton = document.getElementById("submitBooking");
const cancelBookingButton = document.getElementById("cancelBooking");
const bookingMessage = document.getElementById("bookingMessage");

/** Tomorrow in the visitor's own time zone, as YYYY-MM-DD. */
function tomorrow() {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${date.getFullYear()}-${month}-${day}`;
}

async function loadMakes() {
    try {
        const response = await fetch("/api/vehicles/makes");
        if (response.ok) {
            const makes = await response.json();
            addOptions(makeFilter, makes.map((make) => [make, make]));
        }
    } catch {
        // The make list is a convenience; searching still works without it.
    }
}

function renderVehicles(vehicles) {
    vehicleGrid.replaceChildren();
    for (const vehicle of vehicles) {
        const card = document.createElement("article");
        card.className = "vehicle-card";

        const title = document.createElement("h3");
        title.textContent = vehicleName(vehicle);

        const meta = document.createElement("p");
        meta.className = "vehicle-meta";
        meta.textContent = [
            BODY_TYPE_LABELS[vehicle.bodyType] ?? vehicle.bodyType,
            vehicle.color,
            `${numberFormat.format(vehicle.mileage)} miles`,
        ].join(" · ");

        const vin = document.createElement("p");
        vin.className = "vehicle-vin";
        vin.textContent = `VIN ${vehicle.vin}`;

        const price = document.createElement("p");
        price.className = "vehicle-price";
        price.textContent = priceFormat.format(vehicle.price);

        const bookButton = document.createElement("button");
        bookButton.type = "button";
        bookButton.textContent = "Book a test drive";
        bookButton.addEventListener("click", () => openBooking(vehicle));

        card.append(title, meta, vin, price, bookButton);
        vehicleGrid.append(card);
    }
}

async function loadVehicles() {
    // Customers only see vehicles that can be bought or test driven today.
    const params = new URLSearchParams({ status: "AVAILABLE" });
    if (makeFilter.value) {
        params.set("make", makeFilter.value);
    }
    if (bodyTypeFilter.value) {
        params.set("bodyType", bodyTypeFilter.value);
    }
    if (maxPriceFilter.value) {
        params.set("maxPrice", maxPriceFilter.value);
    }

    setStatus(resultsMessage, "Loading vehicles…");
    try {
        const response = await fetch(`/api/vehicles?${params}`);
        if (!response.ok) {
            throw new Error(await readError(response));
        }
        const vehicles = await response.json();
        renderVehicles(vehicles);
        setStatus(resultsMessage, vehicles.length === 0
            ? "No available vehicles match your search."
            : `${vehicles.length} vehicle${vehicles.length === 1 ? "" : "s"} available.`);
    } catch (error) {
        vehicleGrid.replaceChildren();
        setStatus(resultsMessage, `Could not load vehicles: ${error.message}`, "error");
    }
}

function openBooking(vehicle) {
    bookingForm.reset();
    bookingVehicleId.value = vehicle.id;
    bookingVehicle.textContent = `${vehicleName(vehicle)} – ${priceFormat.format(vehicle.price)}`;
    preferredDate.min = tomorrow();
    setStatus(bookingMessage, "");
    bookingForm.hidden = false;
    bookingPanel.hidden = false;
    bookingPanel.scrollIntoView({ behavior: "smooth", block: "start" });
    customerName.focus();
}

function closeBooking() {
    bookingForm.reset();
    bookingPanel.hidden = true;
}

bookingForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    submitBookingButton.disabled = true;
    setStatus(bookingMessage, "Sending your request…");
    try {
        const response = await fetch("/api/test-drives", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                vehicleId: Number(bookingVehicleId.value),
                customerName: customerName.value.trim(),
                email: customerEmail.value.trim(),
                phone: customerPhone.value.trim(),
                preferredDate: preferredDate.value,
            }),
        });
        if (!response.ok) {
            throw new Error(await readError(response));
        }
        const booking = await response.json();
        bookingForm.hidden = true;
        setStatus(bookingMessage,
            `Thank you, ${booking.customerName}! Request #${booking.id} for ${booking.preferredDate} `
                + "has been received. Our team will contact you to confirm.",
            "success");
    } catch (error) {
        setStatus(bookingMessage, `Could not book the test drive: ${error.message}`, "error");
    } finally {
        submitBookingButton.disabled = false;
    }
});

cancelBookingButton.addEventListener("click", closeBooking);

searchForm.addEventListener("submit", (event) => {
    event.preventDefault();
    loadVehicles();
});

resetSearchButton.addEventListener("click", () => {
    searchForm.reset();
    loadVehicles();
});

addOptions(bodyTypeFilter, Object.entries(BODY_TYPE_LABELS));
loadMakes();
loadVehicles();
