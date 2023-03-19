package ui.core

case class Action(label: String, onSelect: () => ViewResult)
