package model.db

trait HasId[Id] {
  def id: Id
}
