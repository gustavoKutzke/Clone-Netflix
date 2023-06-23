package co.tiagoaguiar.netflixremake.model

data class MovieDetail(
    val movie: Movie,
    val similar : List<Movie>
)
