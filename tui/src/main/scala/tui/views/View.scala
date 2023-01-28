package tui.views

trait View[F[_]]:
  def view(): F[Unit]
