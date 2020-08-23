package app.vizion.exampleProject.auth.http.requests

import app.vizion.exampleProject.auth.schema.auth._

case class LoginUser(
    username: UserNameParam,
    password: PasswordParam
)
