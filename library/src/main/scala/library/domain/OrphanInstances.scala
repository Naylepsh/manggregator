package library.domain

import java.util.Date

import cats.kernel.Order

object OrphanInstances:
  given Order[Date] = Order.by(_.getTime())
