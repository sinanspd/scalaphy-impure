package app.vizion.exampleProject.requests

import app.vizion.exampleProject.schema.movies.Genre

case class NewMovieRequest(
    name: String,
    year: Int,
    description: String,
    genre: String
)
