package util.crawl

sealed trait CompareWithExisting

case object NoExistingValue extends CompareWithExisting

case object EqualsExistingValue extends CompareWithExisting

case object NotEqualsExistingValue extends CompareWithExisting