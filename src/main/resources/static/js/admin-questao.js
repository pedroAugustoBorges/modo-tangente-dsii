const editor = document.querySelector("[data-question-editor]");
const alternatives = editor.querySelector("[data-alternatives]");
const optionTemplate = editor.querySelector("[data-option-template]");
const emptyOptions = editor.querySelector("[data-empty-options]");
const addOption = editor.querySelector("[data-add-option]");

function updateOptionFields() {
    const rows = alternatives.querySelectorAll("[data-option]");
    rows.forEach((row, index) => {
        for (const field of row.querySelectorAll("[name]")) {
            field.name = field.name.replace(/\[\d+\]/, `[${index}]`);
        }
        row.querySelector("[data-remove-option]").hidden = false;
    });
    emptyOptions.disabled = rows.length > 0;
}

addOption.addEventListener("click", () => {
    const usedPositions = new Set(Array.from(alternatives.querySelectorAll("[data-option-position]"),
        input => input.valueAsNumber));
    let position = 1;
    while (usedPositions.has(position)) {
        position += 1;
    }
    const row = optionTemplate.content.firstElementChild.cloneNode(true);
    row.querySelector("[data-option-position]").value = position;
    alternatives.append(row);
    updateOptionFields();
    row.querySelector("textarea").focus();
});

alternatives.addEventListener("click", event => {
    const button = event.target.closest("[data-remove-option]");
    if (!button) {
        return;
    }
    const row = button.closest("[data-option]");
    const neighbor = row.nextElementSibling || row.previousElementSibling;
    row.remove();
    updateOptionFields();
    (neighbor?.querySelector("[data-remove-option]") || addOption).focus();
});

alternatives.addEventListener("change", event => {
    const selected = event.target;
    if (selected.matches("[data-option-correct]:checked")) {
        for (const checkbox of alternatives.querySelectorAll("[data-option-correct]")) {
            checkbox.checked = checkbox === selected;
        }
    }
});

updateOptionFields();
addOption.hidden = false;
