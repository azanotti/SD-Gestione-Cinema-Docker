function getFilms() {
    fetch('http://localhost:8080/film')
        .then(response => response.json())
        .then(films => {
            // Display the list of films
            var filmList = document.getElementById('filmList');
            filmList.innerHTML = '';
            films.forEach(film => {
                var listItem = document.createElement('li');
                listItem.innerHTML = film.title;
                listItem.onclick = function () {
                    getFilmDetails(film.id);
                };
                filmList.appendChild(listItem);
            });
        })
        .catch(error => console.error('Error:', error));
}

function getFilmDetails(filmId) {
    fetch(`http://localhost:8080/film/${filmId}`)
        .then(response => response.json())
        .then(film => {
            // Display the film details
            var filmDetails = document.getElementById('filmDetails');
            filmDetails.innerHTML = '';
            var title = document.createElement('h2');
            title.innerHTML = film.title;
            var description = document.createElement('p');
            description.innerHTML = film.description;
            filmDetails.appendChild(title);
            filmDetails.appendChild(description);
        })
        .catch(error => console.error('Error:', error));
}

function addFilm() {
    var title = prompt('Enter the film title:');
    var description = prompt('Enter the film description:');

    if (title && description) {
        var film = {
            title: title,
            description: description
        };

        fetch('http://localhost:8080/film', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(film)
        })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Film added successfully!');
                } else {
                    alert('Failed to add film.');
                }
            })
            .catch(error => console.error('Error:', error));
    }
}

function bookSeats() {
    // Get the selected seats
    var selectedSeats = [];
    var table = document.getElementById('seatTable');
    var rows = table.getElementsByTagName('tr');

    for (var i = 0; i < rows.length; i++) {
        var cells = rows[i].getElementsByTagName('td');

        for (var j = 0; j < cells.length; j++) {
            var seat = cells[j];

            if (seat.classList.contains('selected')) {
                selectedSeats.push(seat.id);
            }
        }
    }

    if (selectedSeats.length > 0) {
        var booking = {
            seats: selectedSeats
        };

        fetch('http://localhost:8080/booking', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(booking)
        })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Seats booked successfully!');
                } else {
                    alert('Failed to book seats.');
                }
            })
            .catch(error => console.error('Error:', error));
    }
}


function displayMovies() {
    // JavaScript code to fetch and display movie data
    const movieListContainer = document.getElementById("movie-list");

    fetch("http://localhost:8080/film")
        .then(response => response.json())
        .then(data => {
            data.forEach(movie => {
                const movieItem = document.createElement("div");
                movieItem.classList.add("movie-item");

                const movieTitle = document.createElement("h3");
                movieTitle.textContent = movie.nome;

                const movieCover = document.createElement("img");
                movieCover.src = movie.immagine;
                movieCover.alt = movie.nome;

                movieItem.appendChild(movieTitle);
                movieItem.appendChild(movieCover);
                movieListContainer.appendChild(movieItem);

                // Add click event listener to movie item
                movieItem.addEventListener("click", () => {
                    // Store selected movie id in session storage
                    sessionStorage.setItem("selectedMovieId", movie.id);

                    // Redirect to the date selection page
                    window.location.href = "date.html";
                });
            });
        })
        .catch(error => {
            console.log("Error fetching movie data:", error);
        });
}

function showSelectedMovieCover() {
    const selectedMovieId = sessionStorage.getItem("selectedMovieId");
    const selectedMovieElement = document.getElementById("selected-movie");

    // Fetch the movie data using the selected movie ID
    fetch("http://localhost:8080/film")
        .then(response => response.json())
        .then(data => {
            const selectedMovie = data.find(movie => movie.id === selectedMovieId);

            // Retrieve the cover image URL and title from the selected movie
            const coverUrl = selectedMovie.immagine;
            const movieTitle = selectedMovie.nome;

            // Create a new selected-movie-item element
            const selectedMovieItem = document.createElement("div");
            selectedMovieItem.classList.add("selected-movie-item");

            // Create an image element for the movie cover
            const movieCoverElement = document.createElement("img");
            movieCoverElement.src = coverUrl;
            movieCoverElement.alt = movieTitle;

            // Create a title element for the movie
            const movieTitleElement = document.createElement("h2");
            movieTitleElement.textContent = `Selected Movie: ${movieTitle}`;

            // Append the movie cover and title to the selected-movie-item
            selectedMovieItem.appendChild(movieCoverElement);
            selectedMovieItem.appendChild(movieTitleElement);

            // Clear the existing content and append the selected-movie-item
            selectedMovieElement.innerHTML = "";
            selectedMovieElement.appendChild(selectedMovieItem);
        })
        .catch(error => {
            console.log("Error fetching movie data:", error);
        });
}

function loadDateOptions() {
    const dateSelectElement = document.getElementById("date-select");
    const selectedMovieId = sessionStorage.getItem("selectedMovieId");

    // Retrieve the projection list for the selected movie
    const projectionListUrl = `http://localhost:8080/proiezione/${selectedMovieId}`;

    // Fetch the projection list data using the selected movie ID
    fetch(projectionListUrl)
        .then(response => response.json())
        .then(data => {
            // Get the unique dates from the projection list
            const uniqueDates = [...new Set(data.map(projection => projection.data))];

            // Clear existing options
            dateSelectElement.innerHTML = "";

            // Create the date options
            uniqueDates.forEach(date => {
                const option = document.createElement("option");
                option.value = date;
                option.textContent = date;
                dateSelectElement.appendChild(option);
            });

            loadTimeOptions();
        })
        .catch(error => {
            console.log("Error fetching projection list data:", error);
        });
}


function loadTimeOptions() {
    const selectedDate = document.getElementById("date-select").value;
    const selectedMovieId = sessionStorage.getItem("selectedMovieId");
    const timePickerElement = document.getElementById("time-picker");

    // Retrieve the projection list for the selected movie and date
    const projectionListUrl = `http://localhost:8080/proiezione/${selectedMovieId}`;

    // Fetch the projection list data using the selected movie ID
    fetch(projectionListUrl)
        .then(response => response.json())
        .then(data => {
            // Filter the projections based on the selected date
            const projections = data.filter(projection => projection.data === selectedDate);

            // Create the time options
            const timeOptions = projections.map(projection => {
                const option = document.createElement("option");
                option.value = projection.ora;
                option.textContent = projection.ora;
                return option;
            });

            // Clear existing options
            timePickerElement.innerHTML = "";

            // Append the time options to the time picker
            timeOptions.forEach(option => {
                timePickerElement.appendChild(option);
            });
        })
        .catch(error => {
            console.log("Error fetching projection list data:", error);
        });
}

// Call loadTimeOptions function when the date select value changes
document.getElementById("date-select").addEventListener("change", loadTimeOptions);

// Call loadTimeOptions function on page load if a date is pre-selected
document.addEventListener("DOMContentLoaded", function() {
    const selectedDate = document.getElementById("date-select").value;
    if (selectedDate) {
        loadTimeOptions();
    }
});

function createSeatElement(seat) {
    const seatElement = document.createElement("div");
    seatElement.className = `seat ${seat.disponibile ? "available" : "unavailable"}`;
    return seatElement;
}

function handleNextButtonClick() {

    // Get the selected date and time
    const selectedDate = document.getElementById("date-select").value;
    const selectedTime = document.getElementById("time-picker").value;

    // Store the selected date and time in session storage
    sessionStorage.setItem("selectedDate", selectedDate);
    sessionStorage.setItem("selectedTime", selectedTime);

    // Retrieve the projection list for the selected movie and date
    const selectedMovieId = sessionStorage.getItem("selectedMovieId");
    const projectionListUrl = `http://localhost:8080/proiezione/${selectedMovieId}`;

    // Fetch the projection list data using the selected movie ID
    fetch(projectionListUrl)
        .then(response => response.json())
        .then(data => {
            // Filter the projections based on the selected date
            console.log(data);
            const projections = data.filter(projection => projection.data === selectedDate);
            const selectedProjection = projections.filter(projection => projection.ora === selectedTime)[0];

            console.log(projections);
            console.log(selectedProjection);
            console.log(selectedProjection.id);
            sessionStorage.setItem("projectionId", selectedProjection.id);
            window.location.href = "seats.html"
        })
        .catch(error => {
            console.log("Error fetching projection list data:", error);
        });
}


function showSeats() {
    const seatsContainer = document.getElementById("seats-container");

    const projectionListUrl = `http://localhost:8080/proiezione/${selectedMovieId}`;

    // Fetch the seat data from the JSON file (replace with your JSON URL)
    fetch(projectionListUrl)
        .then(response => response.json())
        .then(data => {
            // Iterate over the seat data and create seat elements
            data.forEach(seat => {
                const seatElement = document.createElement("div");
                seatElement.className = "seat";
                seatElement.textContent = seat.numero;
                seatElement.style.backgroundColor = getSeatColor(seat);

                seatsContainer.appendChild(seatElement);
            });
        })
        .catch(error => {
            console.log("Error fetching seat data:", error);
        });
}

function getSeatColor(seat) {
    // Logic to determine seat color based on availability
    // Replace with your own logic using seat data properties

    if (seat.available) {
        return "green"; // Available seat color
    } else {
        return "red"; // Unavailable seat color
    }
}