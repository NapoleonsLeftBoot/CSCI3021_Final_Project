const startWord = "CROWN";
const goalWord = "DROPS";

const form = document.getElementById("ladder-form");
const answerRows = document.getElementById("answer-rows");
const submitButton = document.getElementById("submit-word");
const message = document.getElementById("game-message");
const attemptCounter = document.getElementById("attempt-counter");
const resetButton = document.getElementById("reset-game");

//for adding the puzzle
const addPuzzleButton = document.getElementById("add-puzzle");
const addPuzzleContainer = document.getElementById("add-puzzle-container");
const addPuzzleForm = document.getElementById("add-puzzle-form");
const cancelPuzzleButton = document.getElementById("cancel-puzzle");
const addPuzzleMessage = document.getElementById("add-puzzle-message");

let currentRow = 0;
let attempts = 0;
let gameComplete = false;


// Get the word entered in a row.
function getRowWord(row) {
    const inputs = row.querySelectorAll("input");

    return Array.from(inputs)
        .map(input => input.value.toUpperCase())
        .join("");
}


// Check whether all five boxes have a letter.
function isRowComplete(row) {
    const inputs = row.querySelectorAll("input");

    return Array.from(inputs).every(input => input.value !== "");
}


// Check whether exactly one letter is different.
function changesOneLetter(word1, word2) {
    if (word1.length !== 5 || word2.length !== 5) {
        return false;
    }

    let differences = 0;

    for (let i = 0; i < 5; i++) {
        if (word1[i] !== word2[i]) {
            differences++;
        }
    }

    return differences === 1;
}


// Show a message under the puzzle.
function showMessage(text, type = "") {
    message.textContent = text;
    message.className = "game-message";

    if (type !== "") {
        message.classList.add(type);
    }
}


// Update the number of attempts.
function updateAttempts() {
    attemptCounter.textContent = "Attempts: " + attempts;
}


// Add a new row with five letter boxes.
function createAnswerRow() {
    const row = document.createElement("div");
    row.className = "word-row answer-row";
    row.dataset.row = currentRow;

    for (let i = 0; i < 5; i++) {
        const input = document.createElement("input");

        input.type = "text";
        input.maxLength = 1;
        input.setAttribute("aria-label", "Word " + (currentRow + 1) + ", letter " + (i + 1));

        // Only allow one letter in each box.
        input.addEventListener("input", function() {
            input.value = input.value.replace(/[^a-zA-Z]/g, "").toUpperCase().slice(0, 1);
            row.classList.remove("incorrect");

            if (input.value && i < 4) {
                row.querySelectorAll("input")[i + 1].focus();
            }
        });

        // Backspace moves to the previous box.
        input.addEventListener("keydown", function(event) {
            if (event.key === "Backspace" && input.value === "" && i > 0) {
                row.querySelectorAll("input")[i - 1].focus();
            }

            if (event.key === "Enter") {
                event.preventDefault();
                form.requestSubmit();
            }
        });

        row.appendChild(input);
    }

    answerRows.appendChild(row);
    row.querySelector("input").focus();
}


// Check a submitted word.
form.addEventListener("submit", function(event) {
    event.preventDefault();

    if (gameComplete) {
        return;
    }

    const row = answerRows.children[currentRow];
    const word = getRowWord(row);

    if (!isRowComplete(row)) {
        showMessage("Please fill in all five letters.", "error");
        return;
    }

    attempts++;
    updateAttempts();

    let previousWord = startWord;

    if (currentRow > 0) {
        previousWord = getRowWord(answerRows.children[currentRow - 1]);
    }

    if (!changesOneLetter(previousWord, word)) {
        row.classList.add("incorrect");
        showMessage("You must change exactly one letter from the previous word.", "error");
        return;
    }

    // This row is correct, so lock it.
    row.classList.remove("incorrect");
    row.classList.add("correct");

    row.querySelectorAll("input").forEach(function(input) {
        input.disabled = true;
    });

    // Check if the destination word has been reached.
    if (word === goalWord) {
        gameComplete = true;
        showMessage("You win! You completed today's word ladder.", "success");
        submitButton.disabled = true;
        submitButton.textContent = "Puzzle Complete";
        return;
    }

    // Make another row for the next guess.
    currentRow++;
    createAnswerRow();

    showMessage("Correct! Enter the next word.");
});


// Reset the puzzle.
resetButton.addEventListener("click", function() {
    currentRow = 0;
    attempts = 0;
    gameComplete = false;

    answerRows.innerHTML = "";

    submitButton.disabled = false;
    submitButton.textContent = "Submit Word";

    updateAttempts();
    showMessage("Enter your first word to begin!");

    createAnswerRow();
});


// Start with one empty answer row.
createAnswerRow();

// Open the Add Puzzle popup.
addPuzzleButton.addEventListener("click", function() {
    addPuzzleContainer.hidden = false;
    addPuzzleMessage.textContent = "";
});

// Close the popup.
cancelPuzzleButton.addEventListener("click", function() {
    addPuzzleContainer.hidden = true;
});

// Close popup when clicking outside the form.
addPuzzleContainer.addEventListener("click", function(event) {
    if (event.target === addPuzzleContainer) {
        addPuzzleContainer.hidden = true;
    }
});

// Submit a new puzzle to the backend.
addPuzzleForm.addEventListener("submit", async function(event) {
    event.preventDefault();

    addPuzzleMessage.textContent = "Submitting puzzle...";
    addPuzzleMessage.style.color = "#333";

    const formData = new FormData(addPuzzleForm);

    try {
        const response = await fetch("/holidaily/api/tools/addpuzzle", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams(formData)
        });

        const result = await response.json();

        if (response.ok && result.success) {
            addPuzzleMessage.textContent = "Puzzle added successfully!";
            addPuzzleMessage.style.color = "#38866a";
        } else {
            addPuzzleMessage.textContent =
                result.error || "Unable to add puzzle.";
            addPuzzleMessage.style.color = "#b34b4b";
        }

    } catch (error) {
        console.error("Add Puzzle Error:", error);

        addPuzzleMessage.textContent =
            "Something went wrong. Check the browser console.";
        addPuzzleMessage.style.color = "#b34b4b";
    }
});

console.log("Puzzle.js loaded!");

console.log("Add Puzzle button:", addPuzzleButton);
console.log("Popup container:", addPuzzleContainer);