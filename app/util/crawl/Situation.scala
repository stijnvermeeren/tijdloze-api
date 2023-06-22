package util.crawl

sealed trait Situation

case object UniqueValue extends Situation

case object NonUniqueValue extends Situation