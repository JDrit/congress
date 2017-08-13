package net.batchik.congress

import java.io.File
import java.time.Instant

object DataTypes {

  object Party extends Enumeration {
    type Party = Value
    val Republican, Democrat, Independent, Federalist, Anti_Administration, Pro_Administration = Value
  }
  import Party._

  object Gender extends Enumeration {
    type Gender = Value
    val Male, Female = Value
  }
  import Gender._

  case class Sponsor(id: String) extends AnyVal

  case class BillJson(id: String, sponsorId: Sponsor, coSponsorIds: Seq[Sponsor], subject: String, 
		  date: Instant, text: String)

  case class Person(id: String, first: String, last: String, party: Party, state: String,
		    gender: Gender, religion: Option[String])

}
