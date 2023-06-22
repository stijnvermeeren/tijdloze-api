package util.crawl


sealed trait Strategy {
  def decide(situation: Situation, compareWithExisting: CompareWithExisting): Decision
}

object AutoIfUnique extends Strategy {
  def decide(situation: Situation, compareWithExisting: CompareWithExisting): Decision = {
    if (situation == UniqueValue && compareWithExisting != NotEqualsExistingValue) {
      AutoAccept
    } else {
      Pending
    }
  }
}

object AutoOnlyForExistingValue extends Strategy {
  def decide(situation: Situation, compareWithExisting: CompareWithExisting): Decision = {
    if (compareWithExisting == EqualsExistingValue) {
      AutoAccept
    } else {
      Pending
    }
  }
}