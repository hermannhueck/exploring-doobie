package book

import doobie.util.transactor.Transactor
import cats.effect.IO

object transactor {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:world", // connect URL (driver-specific)
    "postgres",              // user
    ""                       // password
  )
}
