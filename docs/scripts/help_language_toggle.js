const flagOptions = document.querySelectorAll(".flag-option");
const recipeBlocks = document.querySelectorAll(".recipe-text");

const setLanguage = (lang) => {
    recipeBlocks.forEach((block) => {
        block.classList.toggle("is-active", block.dataset.lang === lang);
    });
    flagOptions.forEach((btn) => {
        const isActive = btn.dataset.flag === lang;
        btn.classList.toggle("is-selected", isActive);
        btn.setAttribute("aria-pressed", isActive ? "true" : "false");
    });
};

if (flagOptions.length && recipeBlocks.length) {
    setLanguage("de");

    flagOptions.forEach((option) => {
        option.addEventListener("click", () => {
            setLanguage(option.dataset.flag);
        });
    });
}
