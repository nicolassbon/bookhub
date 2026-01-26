// Funcionalidad de cambiar sugerencias
document.querySelector('.change-suggestions').addEventListener('click', function() {
    alert('Cambiar sugerencias: Aquí puedes agregar la lógica para cambiar las sugerencias de libros');
});

// Funcionalidad de "Quiero leerlo"
let wantToReadButtons = document.querySelectorAll('.want-to-read');
wantToReadButtons.forEach(button => {
    button.addEventListener('click', function() {
        alert('Añadido a Quiero leer');
    });
});

// Filtrar comentarios
document.querySelector('.filter-comments').addEventListener('change', function() {
    const filterValue = this.value;
    alert('Filtrando comentarios: ' + filterValue);
});


// botones de like y dislike
document.addEventListener('DOMContentLoaded', function() {
    var likeButton = document.getElementById('like-button');
    var dislikeButton = document.getElementById('dislike-button');
    var likeCount = document.getElementById('like-count');
    var dislikeCount = document.getElementById('dislike-count');

    var liked = false;
    var disliked = false;
    var likeCounter = 0;
    var dislikeCounter = 0;

    likeButton.addEventListener('click', function() {
        if (!liked) {
            likeCounter++;
            if (disliked) {
                dislikeCounter--;
                disliked = false;
                dislikeButton.classList.remove('disliked');
            }
            likeButton.classList.add('liked');
        } else {
            likeCounter--;
            likeButton.classList.remove('liked');
        }
        liked = !liked;
        likeCount.textContent = likeCounter;
        dislikeCount.textContent = dislikeCounter;
    });

    dislikeButton.addEventListener('click', function() {
        if (!disliked) {
            dislikeCounter++;
            if (liked) {
                likeCounter--;
                liked = false;
                likeButton.classList.remove('liked');

            }
            dislikeButton.classList.add('disliked');
        } else {
            dislikeCounter--;
            dislikeButton.classList.remove('disliked');
        }
        disliked = !disliked;
        likeCount.textContent = likeCounter;
        dislikeCount.textContent = dislikeCounter;
    });
});
/*
// Publicar un nuevo comentario
document.querySelector('.submit-comment').addEventListener('click', function() {
    const commentBox = document.querySelector('.comment-box');
    const newComment = commentBox.value.trim();

    if (newComment) {
        alert('Comentario publicado: ' + newComment);
        commentBox.value = ''; // Limpiar la caja de texto
    } else {
        alert('Por favor, escribe un comentario.');
    }
});
*/
document.getElementById('submit-comment').addEventListener('click', function() {
    var commentText = document.getElementById('comment-input').value;
    var userName = document.getElementById("username").textContent = username;
    if (commentText.trim() === "") {
        alert("Por favor, escribí un comentario.");
        return;
    }

    var commentsContainer = document.getElementById('comments-container');

    // Crea un nuevo div para el comentario
    var newComment = document.createElement('div');
    newComment.classList.add('comment');

    // añade comentario
    newComment.innerHTML =
        `    <p class="comment-author">${userName}:</p>
        <p class="comment-text">${commentText}</p>
    `;

    // Añadir el nuevo comentario al contenedor de comentarios
    commentsContainer.appendChild(newComment);

    // Limpiar el campo de texto
    document.getElementById('comment-input').value = "";
});