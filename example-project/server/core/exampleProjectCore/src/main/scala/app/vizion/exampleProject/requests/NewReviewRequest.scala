package app.vizion.exampleProject.requests

import java.util.UUID

case class NewReviewRequest(
    user: UUID,
    body: String,
    date: Long
)
