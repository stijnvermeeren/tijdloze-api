package util.crawl

sealed trait Decision

case object AutoAccept extends Decision

case object Pending extends Decision