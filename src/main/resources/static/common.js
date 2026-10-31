"use strict";

// Shared by the showroom and staff pages.

const BODY_TYPE_LABELS = {
    SEDAN: "Sedan",
    SUV: "SUV",
    HATCHBACK: "Hatchback",
    TRUCK: "Truck",
    COUPE: "Coupe",
    VAN: "Van",
    WAGON: "Wagon",
    CONVERTIBLE: "Convertible",
};

const priceFormat = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
});

const numberFormat = new Intl.NumberFormat("en-US");

/** Reads the API's {"message", "errors"} body, falling back to the status code. */
async function readError(response) {
    try {
        const body = await response.json();
        const fieldErrors = body.errors
            ? Object.entries(body.errors).map(([field, error]) => `${field}: ${error}`).join("; ")
            : "";
        if (body.message && fieldErrors && body.message === "Validation failed") {
            return `${body.message}: ${fieldErrors}`;
        }
        return body.message || fieldErrors || `Request failed (${response.status}).`;
    } catch {
        return `Request failed (${response.status}).`;
    }
}

function vehicleName(vehicle) {
    return [vehicle.modelYear, vehicle.make, vehicle.model, vehicle.trimLevel]
        .filter(Boolean)
        .join(" ");
}

function titleCase(value) {
    return value.charAt(0) + value.slice(1).toLowerCase();
}

function makeBadge(value) {
    const badge = document.createElement("span");
    badge.className = `badge badge-${value.toLowerCase()}`;
    badge.textContent = titleCase(value);
    return badge;
}

function addOptions(select, entries) {
    for (const [value, label] of entries) {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = label;
        select.append(option);
    }
}

function setStatus(element, text, kind) {
    element.textContent = text;
    element.classList.toggle("error", kind === "error");
    element.classList.toggle("success", kind === "success");
}
