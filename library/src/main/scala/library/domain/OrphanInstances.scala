package library.domain

import cats.kernel.Order
import java.util.Date

object OrphanInstances:
  given Order[Date] = Order.by(_.getTime())
