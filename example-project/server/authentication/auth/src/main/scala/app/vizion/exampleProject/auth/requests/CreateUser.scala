package app.vizion.exampleProject.auth.http.requests

import app.vizion.exampleProject.auth.schema.auth._

case class CreateUser(
    username: UserNameParam,
    password: PasswordParam
)
